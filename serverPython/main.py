import asyncio
from asyncio import StreamWriter,StreamReader
import meal
import order
import file_helper
class EchoServer:
    def __init__(self,host="127.0.0.1",port=4004) -> None:
        self.host = host
        self.port = port
        self.server = None
        self.shutdown_event = asyncio.Event() # сделали обьект класса Event
    
    async def handle_client(self,reader:StreamReader,writer:StreamWriter):
        adress = writer.get_extra_info("peername")
        print (f"клиент подключился {adress}")
        try:
            while (True):
                data:bytes = await reader.readline()
                
                if not data:
                    break
                comand = data.decode().strip()
                if comand  == "exit":
                    writer.write("пользователь вышел \n".encode())
                    await writer.drain()
                    break
                if comand =='stop':
                     writer.write("пользователь остановил сервер \n".encode())
                     await writer.drain()
                     self.shutdown_event.set()
                     break
                if comand == "menu":
                    menu = file_helper.FileHelper.get_string_from_file("Menu1.json")
                    meals = file_helper.FileHelper.universal_code_for_parsing(menu, meal.Meal)
                    menu_text = ""
                    for m in meals:
                        menu_text += m.to_json() + " "  # пробелы между

                    menu_text += "\n"  # ← ОБЯЗАТЕЛЬНО \n в конце
                    writer.write(menu_text.encode())
                    await writer.drain()

                if comand == "orders":
                    orders_str = file_helper.FileHelper.get_string_from_file("Orders1.json")
                    orders = file_helper.FileHelper.universal_code_for_parsing(orders_str, order.Order)
                    orders_text = ""
                    for o in orders:
                        orders_text += o.to_json()+ " "
                        writer.write(orders_text.encode())
                        await writer.drain()



                else:
                    writer.write(f"you just write : {comand} \n".encode())
                    await writer.drain()

            pass
        finally:
            writer.close()
            await writer.wait_closed()
            pass

    async def start(self):
        self.server= await asyncio.start_server( # обьект класса Server
            self.handle_client,
            self.host,
            self.port
        )
        """
        1)создаю сервак с параметрами порт и хост, и моей функцией в которую передать Reader-a и
        Writter-a
        2)создаю таски и ставлю параметр до первого выполнения
        serve_task - таска которая записываетя в реестр wait, при создании сервера я указал 
        handle_client , она будет крутиться у пользователя  вечно

        shutdown_task- таска которая 
        """
        print("сервер запущен")
        async with self.server:
            serve_task = asyncio.create_task(self.server.serve_forever())
            shutdown_task = asyncio.create_task(self.shutdown_event.wait())
            #добавь в реестр и запусти в фоне
             
            finished, still_running = await asyncio.wait(
                [serve_task, shutdown_task],
                return_when=asyncio. FIRST_COMPLETED
        ) 
            """
            работает это так что метод wait возращает два значения, законченные и незаконченные
            и та функция которая выполнится первой запишется в finished и код пойдет дальше до цикла 
            и потом закрываем все задачи которые сейчас активны
            """
            for task in still_running:
                task.cancel()

async def main ():
    server = EchoServer()
    await server.start()



if __name__ == "__main__":
    asyncio.run(main())  # запустили программу           