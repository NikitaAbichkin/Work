import asyncio

async def main ():
    while (True):
        try:
            print ("введи айпишник к которому хочешь подключиться")
            serverIP = input("Введи IP: ")  
            reader,writer = await asyncio.open_connection(serverIP,4004)

            while(True):
                comand = input("введи какую нибудь команду: ")
                writer.write((comand+ "\n").encode())
                await writer.drain()
            
                data = await reader.readline()
                if not data:
                    print("Разрыв")
                    break
                
                response = data.decode().strip()
                print (response)
                if "разорвано" in response or "останавливается" in response:
                    break
            writer.close()
            await writer.wait_closed()
        except Exception as e:
            print(f"Ошибка: {e}")
        


if __name__ == "__main__":
    asyncio.run(main())