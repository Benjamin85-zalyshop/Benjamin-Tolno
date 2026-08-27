# Cleanup scripts
import os
files_to_remove = ["patch_dao.py", "patch_repo.py", "patch_dedup.py", "patch_login.py", "patch_dao_fix.py", "patch_years.py", "patch_format.py", "patch_pending_order.py", "patch_chapchap_status.py"]
for f in files_to_remove:
    if os.path.exists(f):
        os.remove(f)
print("Cleanup done")
