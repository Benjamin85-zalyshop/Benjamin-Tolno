import os, signal

pids = [pid for pid in os.listdir('/proc') if pid.isdigit()]

for pid in pids:
    try:
        with open(os.path.join('/proc', pid, 'cmdline'), 'rb') as f:
            cmd = f.read().decode('utf-8').replace('\x00', ' ')
            if 'fix_viewmodel' in cmd and 'kill_all.py' not in cmd:
                print(f"Killing {pid}: {cmd}")
                os.kill(int(pid), signal.SIGKILL)
    except Exception as e:
        pass
print("Done")
