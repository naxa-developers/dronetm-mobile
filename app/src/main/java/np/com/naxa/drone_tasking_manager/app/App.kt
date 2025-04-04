package np.com.naxa.drone_tasking_manager.app

import android.app.Application
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp
import org.gdal.gdal.gdal



@HiltAndroidApp
class App : Application() {

    init {
        gdal.AllRegister()
    }

    override fun onCreate() {
        super.onCreate()

        MMKV.initialize(this)
    }
}
