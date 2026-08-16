import socket
import re
import os

def get_wifi_ip():
    # Try connecting to a public IP address (doesn't send data) to determine active LAN IP
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"

def update_local_properties():
    root_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    local_props_path = os.path.join(root_dir, "local.properties")
    
    current_ip = get_wifi_ip()
    new_url = f"http://{current_ip}:8000/"
    print(f"Detected host LAN IP: {current_ip}")
    print(f"Target BASE_URL: {new_url}")

    if not os.path.exists(local_props_path):
        with open(local_props_path, "w") as f:
            f.write(f"DEBUG_BASE_URL={new_url}\n")
        print(f"Created local.properties with DEBUG_BASE_URL={new_url}")
        return

    with open(local_props_path, "r") as f:
        lines = f.readlines()

    updated = False
    new_lines = []
    for line in lines:
        if line.startswith("DEBUG_BASE_URL="):
            new_lines.append(f"DEBUG_BASE_URL={new_url}\n")
            updated = True
        else:
            new_lines.append(line)

    if not updated:
        new_lines.append(f"DEBUG_BASE_URL={new_url}\n")

    with open(local_props_path, "w") as f:
        f.writelines(new_lines)
    
    print(f"Updated {local_props_path} successfully!")

if __name__ == "__main__":
    update_local_properties()
