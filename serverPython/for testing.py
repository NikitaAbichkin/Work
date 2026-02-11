

# import asyncio
# import time


# async def chat (event:asyncio.Event):
#     while not event.is_set():
#         message = await asyncio.to_thread(input, "write something: ")
#         if message == "bye":
#             print ("пока")
#             event.set()
#         print(f"Бот: Ты написал '{message}'")


# async def wait_for_exit(event):
#     print ("AAAAAAAAAAAA")

#     #функция asyncio.to_thread ( туда записывать те функции которые стопорят или не имеют асинхронности)


# async def main():
#    event=asyncio.Event()
#    await asyncio.gather(chat(event), wait_for_exit(event))

# asyncio.run(main())




