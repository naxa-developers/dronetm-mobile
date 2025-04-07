package np.com.naxa.drone_tasking_manager.features.tasks.utils.flight_plan_creator

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import org.gdal.gdal.gdal
import org.gdal.gdalconst.gdalconstConstants
import org.gdal.ogr.Feature
import org.gdal.ogr.FieldDefn
import org.gdal.ogr.ogr
import org.gdal.osr.SpatialReference
import org.gdal.osr.osr
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter


object Elevation {

    fun addFromRasterDem(rasterPath: String, geoJson: String, outfile: String): String? {
        return try {
            // Insure all gdal and ogr bindings are registered
            gdal.AllRegister()
            ogr.RegisterAll()

            // Open the raster file (DEM)
            val dataset = gdal.Open(rasterPath, gdalconstConstants.GA_ReadOnly)

            // Create an empty Spatial Reference object
            // and set it to the CRS of the input raster
            val rasterSr = SpatialReference()
            rasterSr.ImportFromProj4(dataset.GetProjection())

            // Get the raster band (if it's a DEM, this should be the only band)
            val band = dataset.GetRasterBand(1)

            // Get raster transformation parameters
            val forwardTransform = dataset.GetGeoTransform()
            val reverseTransform = gdal.InvGeoTransform(forwardTransform)

            // Opening provided geoJson file and getting layer data
            val driver = ogr.GetDriverByName("GeoJSON")
            val dataSource = driver.Open(geoJson, 0)
            val layer = dataSource.GetLayer(0)
            val layerSr = layer.GetSpatialRef()
            val layerLd = layer.GetLayerDefn()

            // Create the coordinate transformation
            val transform = osr.CreateCoordinateTransformation(layerSr, rasterSr)

            // Creating temp file for geojson seq
            val tempFile = File.createTempFile("temp_geojsonseq_file", ".geojsonseq")

            // Create the GDAL GeoJSON output file infrastructure
            val outfileDriver = ogr.GetDriverByName("GeoJSONSeq")
            val outfileDataSource = outfileDriver.CreateDataSource(tempFile.absolutePath)
            val outfileLayer = outfileDataSource.CreateLayer("waypoints", layerSr, ogr.wkbPoint)

            // Add an elevation field for easy reference (the point is XYZ, but
            // it's nice to have access to the elevation as a property)
            val elevationField = FieldDefn("elevation", ogr.OFTReal)
            outfileLayer.CreateField(elevationField)

            val fields = mutableListOf<FieldDefn>()

            for (i in 0 until layerLd.GetFieldCount()) {
                val fieldDefn = layerLd.GetFieldDefn(i)
                fields.add(fieldDefn)
            }

            for (field in fields) {
                outfileLayer.CreateField(field)
            }

            // Get the feature definition
            val featureDefn = outfileLayer.GetLayerDefn()

            // Raster data type
            val typeByName = gdal.GetDataTypeName(band.dataType)
            val dataType = rasterDataFormatString(typeByName)

            for (i in 0 until layer.GetFeatureCount()) {
                val feature = layer.GetFeature(i)
                val geom = feature.GetGeometryRef()
                val x = geom.GetX()
                val y = geom.GetY()

                val pointXYRasterCrs = transform.TransformPoint(x, y)

                // Convert geographic coordinates to pixel coordinates
                val geoX = DoubleArray(2)
                val geoY = DoubleArray(2)
                gdal.ApplyGeoTransform(
                    reverseTransform,
                    pointXYRasterCrs[1],
                    pointXYRasterCrs[0],
                    geoX,
                    geoY
                )

                val pixX = geoX[0].toInt()
                val pixY = geoY[1].toInt()

                var elevation = 0.0

                // Check if the point is within raster bounds
                if (pixX in 0 until dataset.rasterXSize && pixY in 0 until dataset.rasterYSize) {
                    try {
                        when (dataType) {
                            "b" -> {
                                val elevationArray = ByteArray(1)
                                band.ReadRaster(pixX, pixY, 1, 1, elevationArray)
                                elevation = elevationArray[0].toDouble()
                            }

                            "<h" -> {
                                val elevationArray = ShortArray(1)
                                band.ReadRaster(pixX, pixY, 1, 1, elevationArray)
                                elevation = elevationArray[0].toDouble()
                            }

                            "l" -> {
                                val elevationArray = IntArray(1)
                                band.ReadRaster(pixX, pixY, 1, 1, elevationArray)
                                elevation = elevationArray[0].toDouble()
                            }

                            "q" -> {
                                val elevationArray = IntArray(1)
                                band.ReadRaster(pixX, pixY, 1, 1, elevationArray)
                                elevation = elevationArray[0].toDouble()
                            }

                            "f" -> {
                                val elevationArray = FloatArray(1)
                                band.ReadRaster(pixX, pixY, 1, 1, elevationArray)
                                elevation = elevationArray[0].toDouble()
                            }

                            "d" -> {
                                val elevationArray = DoubleArray(1)
                                band.ReadRaster(pixX, pixY, 1, 1, elevationArray)
                                elevation = elevationArray[0]
                            }
                        }
                    } catch (_: Exception) {
                        elevation = 0.0
                    }
                }

                // Create new feature with elevation
                val newPoint = GeometryFactory().createPoint(Coordinate(x, y, elevation))
                val outFeature = Feature(featureDefn)
                outFeature.SetGeometry(ogr.CreateGeometryFromWkt(newPoint.toText()))
                outFeature.SetField("elevation", elevation)

                for (fd in fields) {
                    val fieldName = fd.GetName()

                    val value: Any? = try {
                        feature.GetFieldAsInteger(fieldName)
                    } catch (_: Exception) {
                        try {
                            feature.GetFieldAsString(fieldName)
                        } catch (_: Exception) {
                            try {
                                feature.GetFieldAsDouble(fieldName)
                            } catch (_: Exception) {
                                null // No valid conversion
                            }
                        }
                    }

                    when (value) {
                        is Int -> outFeature.SetField(fieldName, value)
                        is String -> outFeature.SetField(fieldName, value)
                        is Double -> outFeature.SetField(fieldName, value)
                    }
                }

                // Required for GeoJSONSeq: add the feature to the output layer
                outfileLayer.CreateFeature(outFeature)
            }

            // Create output GeoJSON in case of converting file contents
            // val geojsonDriver = ogr.GetDriverByName("GeoJSON")
            // val outDatasource = geojsonDriver.CreateDataSource(outfile)
            // outDatasource.CopyLayer(outfileLayer, outfileLayer.GetName())

            convertGeoJSONSeqManually(tempFile.absolutePath, outfile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun convertGeoJSONSeqManually(inputPath: String, outputPath: String): String {
        val mapper = ObjectMapper()

        FileInputStream(inputPath).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                val featuresArray: ArrayNode = mapper.createArrayNode()

                reader.lineSequence()
                    .filter { it.trim().isNotEmpty() }
                    .forEach { line ->
                        try {
                            val feature: JsonNode = mapper.readTree(line.trim())
                            featuresArray.add(feature)
                        } catch (_: Exception) {
                        }
                    }

                val featureCollection = mapper.createObjectNode().apply {
                    put("type", "FeatureCollection")
                    set<ArrayNode>("features", featuresArray)
                }

                FileOutputStream(outputPath).use { outputStream ->
                    BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                        mapper.writerWithDefaultPrettyPrinter()
                            .writeValue(writer, featureCollection)
                    }
                }
            }
        }

        return outputPath
    }


    // fun addFromRasterDem1(rasterPath: String, geoJson: String, outfile: String): String? {
    //     return try {
    //         // Insure all gdal and ogr bindings are registered
    //         gdal.AllRegister()
    //         ogr.RegisterAll()

    //         // Open the raster file (DEM)
    //         val dataset = gdal.Open(rasterPath, gdalconstConstants.GA_ReadOnly)

    //         // Create an empty Spatial Reference object
    //         // and set it to the CRS of the input raster
    //         val rasterSr = SpatialReference()
    //         rasterSr.ImportFromProj4(dataset.GetProjection())

    //         // Get the raster band (if it's a DEM, this should be the only band)
    //         val band = dataset.GetRasterBand(1)

    //         // Get raster transformation parameters
    //         val forwardTransform = dataset.GetGeoTransform()
    //         val reverseTransform = gdal.InvGeoTransform(forwardTransform)

    //         // Opening provided geoJson file and getting layer data
    //         val driver = ogr.GetDriverByName("GeoJSON")
    //         val dataSource = driver.Open(geoJson, 0)
    //         val layer = dataSource.GetLayer(0)
    //         val layerSr = layer.GetSpatialRef()
    //         val layerLd = layer.GetLayerDefn()

    //         // Create the coordinate transformation
    //         val transform = osr.CreateCoordinateTransformation(layerSr, rasterSr)

    //         // Create the GDAL GeoJSON output file infrastructure
    //         val outfileDriver = ogr.GetDriverByName("GeoJSON")
    //         val outfileDataSource = outfileDriver.CreateDataSource(outfile)
    //         val outfileLayer = outfileDataSource.CreateLayer("waypoints", layerSr, ogr.wkbPoint)

    //         // Add an elevation field for easy reference (the point is XYZ, but
    //         // it's nice to have access to the elevation as a property)
    //         val elevationField = FieldDefn("elevation", ogr.OFTReal)
    //         outfileLayer.CreateField(elevationField)

    //         val fields = mutableListOf<FieldDefn>()

    //         for (i in 0 until layerLd.GetFieldCount()) {
    //             val fieldDefn = layerLd.GetFieldDefn(i)
    //             fields.add(fieldDefn)
    //         }

    //         for (field in fields) {
    //             outfileLayer.CreateField(field)
    //         }

    //         // Get the feature definition
    //         val featureDefn = outfileLayer.GetLayerDefn()

    //         // Raster data type
    //         val typeByName = gdal.GetDataTypeName(band.dataType)
    //         val dataType = rasterDataFormatString(typeByName)


    //         for (i in 0 until layer.GetFeatureCount()) {
    //             val feature = layer.GetFeature(i)
    //             val geom = feature.GetGeometryRef()
    //             val x = geom.GetX()
    //             val y = geom.GetY()

    //             val pointXYRasterCrs = transform.TransformPoint(x, y)

    //             // Convert geographic coordinates to pixel coordinates
    //             val geoX = DoubleArray(1)
    //             val geoY = DoubleArray(1)
    //             gdal.ApplyGeoTransform(
    //                 reverseTransform,
    //                 pointXYRasterCrs[1],
    //                 pointXYRasterCrs[0],
    //                 geoX,
    //                 geoY
    //             )

    //             val pixX = geoX[0].toInt()
    //             val pixY = geoY[1].toInt()

    //             var elevation = 0.0

    //             // Check if the point is within raster bounds
    //             if (pixX in 0 until dataset.rasterXSize && pixY in 0 until dataset.rasterYSize) {
    //                 try {
    //                     when (rasterDataFormatString(dataType)) {
    //                         "b" -> {
    //                             val elevationArray = ByteArray(1)
    //                             band.ReadRaster(pixX, pixY, 1, 1, elevationArray)
    //                             elevation = elevationArray[0].toDouble()
    //                         }

    //                         "<h" -> {
    //                             val elevationArray = ShortArray(1)
    //                             band.ReadRaster(pixX, pixY, 1, 1, elevationArray)
    //                             elevation = elevationArray[0].toDouble()
    //                         }

    //                         "l" -> {
    //                             val elevationArray = IntArray(1)
    //                             band.ReadRaster(pixX, pixY, 1, 1, elevationArray)
    //                             elevation = elevationArray[0].toDouble()
    //                         }

    //                         "q" -> {
    //                             val elevationArray = IntArray(1)
    //                             band.ReadRaster(pixX, pixY, 1, 1, elevationArray)
    //                             elevation = elevationArray[0].toDouble()
    //                         }

    //                         "f" -> {
    //                             val elevationArray = FloatArray(1)
    //                             band.ReadRaster(pixX, pixY, 1, 1, elevationArray)
    //                             elevation = elevationArray[0].toDouble()
    //                         }

    //                         "d" -> {
    //                             val elevationArray = DoubleArray(1)
    //                             band.ReadRaster(pixX, pixY, 1, 1, elevationArray)
    //                             elevation = elevationArray[0]
    //                         }
    //                     }
    //                 } catch (e: Exception) {
    //                     elevation = 0.0
    //                 }

    //             }

    //             // Create new feature with elevation
    //             val newPoint = GeometryFactory().createPoint(Coordinate(x, y, elevation))
    //             val outFeature = Feature(featureDefn)
    //             outFeature.SetGeometry(ogr.CreateGeometryFromWkt(newPoint.toText()))
    //             outFeature.SetField("elevation", elevation)

    //             for (fd in fields) {
    //                 val fieldName = fd.GetName()

    //                 val value: Any? = try {
    //                     feature.GetFieldAsInteger(fieldName)
    //                 } catch (e: Exception) {
    //                     try {
    //                         feature.GetFieldAsString(fieldName)
    //                     } catch (e: Exception) {
    //                         try {
    //                             feature.GetFieldAsDouble(fieldName)
    //                         } catch (e: Exception) {
    //                             null // No valid conversion
    //                         }
    //                     }
    //                 }

    //                 when (value) {
    //                     is Int -> outFeature.SetField(fieldName, value)
    //                     is String -> outFeature.SetField(fieldName, value)
    //                     is Double -> outFeature.SetField(fieldName, value)
    //                 }
    //             }

    //         }
    //         outfile
    //     } catch (e: Exception) {
    //         Log.d("AMIT", "create: ${e.message}")
    //         e.printStackTrace()
    //         null
    //     }
    // }

    private fun rasterDataFormatString(inputDataType: String): String {
        val typeMap = mapOf(
            "Byte" to "b",
            "Int8" to "b",
            "UInt16" to "<H",
            "Int16" to "<h",
            "UInt32" to "L",
            "Int32" to "l",
            "UInt64" to "q",
            "Int64" to "Q",
            "Float32" to "f",
            "Float64" to "d"
        )

        return typeMap[inputDataType]
            ?: throw IllegalArgumentException("Unsupported data type: $inputDataType")
    }

}
