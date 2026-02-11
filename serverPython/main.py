import asyncio
from asyncio import StreamWriter,StreamReader,WriteTransport
import meal
import order
import file_helper
from file_helper import FileHelper

import json
host = "0.0.0.0"
port = 4004

async def send_response(writer:StreamWriter,data=None):
    response = {}
    if data is not None:
        response["data"] = data
    writer.write((json.dumps(response,ensure_ascii=False) + "\n").encode())
    await writer.drain()

    


async def handle_client(reader:StreamReader,writer:StreamWriter,shutdown_event):
    adress = writer.get_extra_info("peername")
    print (f"клиент подключился {adress}")
    try:
        while (True):
            data:bytes = await reader.readline()
            
            if not data:
                break
           
            row = data.decode().strip()
            try:
                request = json.loads(row)
                command = request["command"]
            except(json.JSONDecodeError,KeyError):
                writer.write("неверный формат\n".encode())
                await writer.drain()
                continue
            if command  == "exit":
                writer.write("пользователь вышел \n".encode())
                await writer.drain()
                break
            elif command =='stop':
                    writer.write("пользователь остановил сервер \n".encode())
                    await writer.drain()
                    shutdown_event.set()
                    break
            elif command == "help":
                help_info = [
                    {"command": "help", "data": "нет", "описание": "показать список команд"},
                    {"command": "menu", "data": "нет", "описание": "получить список блюд"},
                    {"command": "orders", "data": "нет", "описание": "получить список заказов"},
                    {"command": "add_meal_to_menu", "data": {"id": "Суп3", "title": "Щи", "cost": 200}, "описание": "добавить блюдо в меню"},
                    {"command": "update_meal_from_menu", "data": {"id": "Суп3", "title": "Щи острые", "cost": 250}, "описание": "обновить блюдо по id"},
                    {"command": "delete_meal_from_menu", "data": {"index": "Суп3"}, "описание": "удалить блюдо по id"},
                    {"command": "exit", "data": "нет", "описание": "отключиться от сервера"},
                    {"command": "stop", "data": "нет", "описание": "остановить сервер"},
                ]
                await send_response(writer, help_info)

            elif command == "menu":
                menu = file_helper.FileHelper.get_string_from_file("Menu1.json")
                meals = json.loads(menu)
                await send_response(writer,meals)
                
                
            elif command == "orders":
                orders = FileHelper.get_string_from_file("Orders1.json")
                allOrders = json.loads(orders)
                await send_response(writer,allOrders)
                

            elif command =="delete_meal_from_menu":
                payload  = request["data"]
                index = payload["index"]
                FileHelper.delete_meal_from_menu(index)
                menu = file_helper.FileHelper.get_string_from_file("Menu1.json")
                meals = json.loads(menu)
                await send_response(writer,meals)
                
            elif command == "update_meal_from_menu":
                payload = request["data"]
                FileHelper.update_meal_from_menu(payload)
                await send_response(writer,FileHelper.load_menu())

            elif command == "add_meal_to_menu":
                payload = request["data"]
                FileHelper.add_meal_to_menu(payload)
                await send_response(writer,FileHelper.load_menu())
                


        
            else:
                writer.write(f"you just write : {command} \n".encode())
                await writer.drain()
    finally:
        writer.close()
        await writer.wait_closed()
        pass

async def start(shutdown_event):
    async def client_handler(reader, writer):
        await handle_client(reader, writer, shutdown_event)
    server= await asyncio.start_server( # обьект класса Server
        client_handler,
        host,
        port
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
    async with server:
        serve_task = asyncio.create_task(server.serve_forever()) # начало прослушиваня
        shutdown_task = asyncio.create_task(shutdown_event.wait())
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
    shutdown_event = asyncio.Event()
    await start(shutdown_event)



if __name__ == "__main__":
    asyncio.run(main())  # запустили программу           



    """
     1)если программа маин то она начинается
     2) в функкции маин создается обьект класса Event
     3) запускае функцию start  с параметром (shutdown_event) 
     4) вызываю функцию  client_handler которая вызывает handle_client с параметрами 
     (reader,writer,shutdown_event)
     потому что при создании сервера прописывается функция которая запуститься в начале  и куда передать 
     два обьекта класов Reader, Writer, но нам нужно еще передать параметр Event, поэтому мы сделали некую
     обертку
     5) Создаем  сервер с нашей оберткой
     6) Внутри handle_client бесконечный цикл внутри   где выполняется прослушивание порта  через 
     reader и цикл останаливается только тогда когда написано   stop  Event.set() 
     так как у нас идет асинхронное выполнение serve_task и shutdown_task и wait ждет пока
     выполнится кто то первый а они оба бесконечны, потому что пока чел не напишет 'stop'
     или пока бесконечная функция прослушивания закончится(никогда)
     потом когда заканчиватеся одна из них все остальные закрываются
    """