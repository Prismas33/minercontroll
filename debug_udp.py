#!/usr/bin/env python3
import socket
import json
import time
from datetime import datetime

def capture_udp_packets():
    # Criar socket UDP
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.bind(('0.0.0.0', 12345))  # Porta padrão dos miners
    
    print(f"[{datetime.now()}] Listening for UDP packets on port 12345...")
    print("=" * 80)
    
    miners_found = {}
    
    while True:
        try:
            # Receber dados
            data, addr = sock.recvfrom(4096)
            
            # Decodificar
            message = data.decode('utf-8').strip()
            
            # Log do IP e dados brutos
            print(f"\n[{datetime.now()}] Packet from {addr[0]}:{addr[1]}")
            print(f"Raw data: {repr(message)}")
            print("-" * 40)
            
            # Tentar parse JSON
            try:
                json_data = json.loads(message)
                print("Parsed JSON:")
                print(json.dumps(json_data, indent=2, ensure_ascii=False))
                
                # Guardar dados únicos por IP
                miners_found[addr[0]] = json_data
                
                print(f"\nFields found:")
                for key, value in json_data.items():
                    print(f"  {key}: {value} (type: {type(value).__name__})")
                    
            except json.JSONDecodeError as e:
                print(f"JSON decode error: {e}")
                print(f"Trying to parse as plain text...")
                
            print("=" * 80)
            
            # Mostrar sumário dos miners encontrados
            if len(miners_found) > 0:
                print(f"\nMINERS SUMMARY ({len(miners_found)} found):")
                for ip, data in miners_found.items():
                    name = data.get('Name', data.get('name', 'Unknown'))
                    print(f"  {ip}: {name}")
                print()
                
        except KeyboardInterrupt:
            print("\n\nCapture stopped by user")
            break
        except Exception as e:
            print(f"Error: {e}")
            continue
    
    # Mostrar resultado final
    print("\n" + "="*80)
    print("FINAL ANALYSIS")
    print("="*80)
    
    for ip, data in miners_found.items():
        print(f"\nMiner IP: {ip}")
        print(f"Complete JSON: {json.dumps(data, indent=2, ensure_ascii=False)}")
        
        # Analisar campos específicos
        fields_analysis = {
            'Power': ['Power', 'power', 'PowerConsumption', 'Watts', 'watts'],
            'Temperature': ['Temp', 'temp', 'Temperature', 'temperature'],
            'Shares': ['Share', 'share', 'Shares', 'shares', 'AcceptedShares'],
            'Valid': ['Valid', 'valid', 'ValidShares', 'Accepted', 'accepted'],
            'HashRate': ['HashRate', 'hashrate', 'Hashrate', 'Speed', 'speed']
        }
        
        print("\nField Analysis:")
        for field_type, possible_names in fields_analysis.items():
            found = False
            for name in possible_names:
                if name in data:
                    print(f"  {field_type}: Found as '{name}' = {data[name]}")
                    found = True
                    break
            if not found:
                print(f"  {field_type}: NOT FOUND")
    
    sock.close()

if __name__ == "__main__":
    print("UDP Miner Packet Capture Tool")
    print("Network: GXAQE4GH")
    print("Press Ctrl+C to stop")
    capture_udp_packets()
