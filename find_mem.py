import re
import sys
import os

pid = 706
maps_file = f"/proc/{pid}/maps"
mem_file = f"/proc/{pid}/mem"

# The string we are looking for:
target_string = b"import androidx.compose.material3.*"
# Or a longer one:
target_string = b"class SchoolViewModel("

def search():
    with open(maps_file, 'r') as map_f, open(mem_file, 'rb') as mem_f:
        for line in map_f:
            parts = line.split()
            if not parts: continue
            if 'r' not in parts[1]: continue
            
            start_end = parts[0].split('-')
            start = int(start_end[0], 16)
            end = int(start_end[1], 16)
            size = end - start
            
            try:
                mem_f.seek(start)
                chunk = mem_f.read(size)
                
                idx = chunk.find(target_string)
                if idx != -1:
                    print(f"Found in region {line.strip()} at index {idx}")
                    # Save a 1MB chunk around it
                    start_dump = max(0, idx - 500000)
                    end_dump = min(len(chunk), idx + 500000)
                    with open(f"/tmp/dump_{start:x}.bin", "wb") as dump_f:
                        dump_f.write(chunk[start_dump:end_dump])
                    
            except Exception as e:
                pass

if __name__ == "__main__":
    search()
