import re
import sys
import os

pid = 706
maps_file = f"/proc/{pid}/maps"
mem_file = f"/proc/{pid}/mem"

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
            if size > 100000000: # skip regions > 100MB
                continue
                
            try:
                mem_f.seek(start)
                chunk = mem_f.read(size)
                
                idx = 0
                while True:
                    idx = chunk.find(target_string, idx)
                    if idx == -1: break
                    print(f"Found in region {line.strip()} at index {idx}")
                    
                    # Search around it for the boundaries of the file
                    # We know the file starts with "package com.example.ui"
                    # and ends with "}\n" or something, around 33000 bytes long.
                    start_search = max(0, idx - 40000)
                    end_search = min(len(chunk), idx + 40000)
                    sub = chunk[start_search:end_search]
                    
                    # find all "package com.example"
                    pkg_idx = sub.rfind(b"package com.example")
                    if pkg_idx != -1:
                        # find the end of the file. Let's just dump the block.
                        with open(f"/tmp/full_recovered_{start:x}_{idx}.txt", "wb") as out:
                            out.write(sub[pkg_idx:])
                        print(f"Dumped full_recovered_{start:x}_{idx}.txt")
                        
                    idx += 1
            except Exception as e:
                pass

if __name__ == "__main__":
    search()
