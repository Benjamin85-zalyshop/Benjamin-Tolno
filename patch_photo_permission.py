import re
with open('app/src/main/java/com/example/ui/components/PhotoSourceDialog.kt', 'r') as f:
    content = f.read()

imports = """import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
"""
content = content.replace('import androidx.compose.runtime.*', 'import androidx.compose.runtime.*\n' + imports)

# We need to add the permission launcher and the check
permission_logic = """
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            android.widget.Toast.makeText(context, "Permission refusée", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
"""

content = content.replace('val galleryLauncher =', permission_logic + '\n    val galleryLauncher =')

camera_click = """onClick = {
                        if (hasCameraPermission) {
                            cameraLauncher.launch(null)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }"""

content = re.sub(r'onClick = \{\s*cameraLauncher\.launch\(null\)\s*\}', camera_click, content)

with open('app/src/main/java/com/example/ui/components/PhotoSourceDialog.kt', 'w') as f:
    f.write(content)
