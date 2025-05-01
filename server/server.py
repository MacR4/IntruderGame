#!/usr/bin/env python3

import socket
import threading

# Server configuration
HOST = '127.0.0.1'
PORT = 4300

clients = {}  # Dict to store {client_id: socket}

def handle_client(client_socket, address):
    """Handles a new client connection."""
    print(f"[+] New connection from {address}")

    try:
        # First message must be client ID
        raw = client_socket.recv(1024)
        if not raw:
            return
        connect_msg = raw.decode('utf-8').strip()
        if not connect_msg.startswith("CONNECT "):
            print("[-] Expected CONNECT message. Closing connection.")
            client_socket.close()
            return

        client_id = connect_msg.split()[1]
        print(f"[+] Client ID assigned: {client_id}")
        clients[client_id] = client_socket

        # Listen for messages from this client
        while True:
            data = client_socket.recv(1024)
            if not data:
                break
            msg = data.decode('utf-8').strip()
            print(f"[{client_id}]: {msg}")
            broadcast(f"{msg}".encode('utf-8'), exclude_id=client_id)
    except ConnectionResetError:
        print(f"[-] Connection lost from {address}")
    finally:
        print(f"[!] Removing client: {client_id}")
        clients.pop(client_id, None)
        client_socket.close()

def broadcast(message, exclude_id=None):
    """Broadcast a message to all clients except the sender (exclude_id)."""
    for cid, sock in clients.items():
        if cid != exclude_id:
            try:
                sock.send(message)
            except Exception as e:
                print(f"[!] Error sending to {cid}: {e}")
                sock.close()
                clients.pop(cid, None)

def start_server():
    """Starts the TCP server and listens for new connections."""
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.bind((HOST, PORT))
    server.listen(5)
    print(f"[*] Server listening on {HOST}:{PORT}")
    while True:
        client_socket, address = server.accept()
        threading.Thread(target=handle_client, args=(client_socket, address)).start()

if __name__ == "__main__":
    start_server()
