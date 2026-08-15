import re

with open('/tmp/dump_7fbcdc000000.bin', 'rb') as f:
    data = f.read()

# ascii strings length >= 1000
ascii_strings = re.findall(b'[\x20-\x7E\x09\x0A\x0D]{1000,}', data)
for idx, s in enumerate(ascii_strings):
    if b"class SchoolViewModel" in s:
        with open(f"/tmp/recovered_ascii_{idx}.txt", 'wb') as out:
            out.write(s)
            
# utf-16 strings length >= 1000 (every other byte is 0)
utf16_pattern = b'(?:[\x20-\x7E\x09\x0A\x0D]\x00){1000,}'
utf16_strings = re.findall(utf16_pattern, data)
for idx, s in enumerate(utf16_strings):
    if b"c\x00l\x00a\x00s\x00s\x00 \x00S\x00c\x00h\x00o\x00o\x00l" in s:
        with open(f"/tmp/recovered_utf16_{idx}.txt", 'wb') as out:
            out.write(s)
            
print("Ascii:", len(ascii_strings), "UTF-16:", len(utf16_strings))
