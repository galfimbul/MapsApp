package ru.aevshvetsov.testproject.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ru.aevshvetsov.testproject.R
import ru.aevshvetsov.testproject.ui.fragments.MainScreen
import ru.aevshvetsov.testproject.utils.REQUEST_PERMISSIONS_REQUEST_CODE

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val requiredPermissions =
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        checkPermissions(requiredPermissions)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val permissionsToRequest = mutableListOf<String>()
        if (grantResults.isNotEmpty()) {

            grantResults.forEachIndexed { index, status ->
                if (status == PackageManager.PERMISSION_DENIED) {
                    showToast("Нет необходимого разрешения. Предоставьте разрешение: ${permissions[index]}")
                    permissionsToRequest.add(permissions[index])
                }
            }
        }
        if (permissionsToRequest.isEmpty()){
            setFragmentWithoutBackStack(MainScreen.newInstance())
        } else{
            showToast("Нет необходимых разрешений. Проверьте настройки доступа.")
            ActivityCompat.requestPermissions(
                this, permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissions(permissions: List<String>) {
        val permissionsToRequest = mutableListOf<String>()
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this, permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        } else {
            setFragmentWithoutBackStack(MainScreen.newInstance())
        }
    }

    private fun setFragmentWithoutBackStack(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, fragment)
            .commit()
    }
}
