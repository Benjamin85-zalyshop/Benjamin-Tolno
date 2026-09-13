import re

filepath = 'app/src/main/java/com/example/ui/SchoolViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

# We will use regex to find the end of saveGrade safely
# saveGrade signature: fun saveGrade(...
# We look for the closing brace of the launch block inside saveGrade.

# Let's just find:
# .addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
#         }
#     }
# And ONLY replace the ONE inside saveGrade.

old_block = """            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
        }
    }"""

new_block = """            ).addOnFailureListener { e -> android.util.Log.e("ScolaPay-Firebase", "Error syncing to Firebase", e) }
            
            // Wait for DB insertion and then sync RTDB
            syncStudentAcademicsToRTDB(schoolId, studentId, term)
        }
    }"""

# The problem last time was that there are MULTIPLE such blocks.
# In `fix_unresolved3.py`, I deleted ALL of them, and then in `fix_save_grade_again.py`, maybe the exact string didn't match.

