with open('/tmp/dump_7fbcdc000000.bin', 'rb') as f:
    data = f.read()

idx = data.find(b"class SchoolViewModel(")
print("Found at", idx)
start = max(0, idx - 2000)
end = min(len(data), idx + 2000)
print(data[start:end].decode('utf-8', errors='ignore'))
