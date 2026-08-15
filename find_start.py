import os

pid = 706
maps_file = f"/proc/{pid}/maps"
mem_file = f"/proc/{pid}/mem"

target_string = b"import androidx.compose.material3"

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
            if size > 100000000: continue
                
            try:
                mem_f.seek(start)
                chunk = mem_f.read(size)
                
                idx = 0
                while True:
                    idx = chunk.find(target_string, idx)
                    if idx == -1: break
                    print(f"Found at {line.strip()} index {idx}")
                    with open(f"/tmp/pkg_{start:x}_{idx}.txt", "wb") as out:
                        out.write(chunk[max(0, idx-1000) : min(len(chunk), idx+33000)])
                    idx += 1
            except Exception as e:
                pass

if __name__ == "__main__":
    search()
