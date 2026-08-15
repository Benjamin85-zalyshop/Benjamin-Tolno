with open('/tmp/dump_7fbcdc000000.bin', 'rb') as f:
    data = f.read()
idx = data.find(b"class SchoolViewModel(")
print(data[idx-500:idx+500].decode('utf-8', errors='ignore'))
