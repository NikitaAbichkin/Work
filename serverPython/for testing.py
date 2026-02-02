

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




# The client gets the API key from the environment variable `GEMINI_API_KEY`.
from google import genai

# Передаем ключ напрямую в конструктор (создатель объекта)
client = genai.Client(api_key="AIzaSyDk_XDdduwwyhnT6h9RFdcw9y1PyY1Gudw")

response = client.models.generate_content(
    model="gemini-2.5-flash", # Советую юзать 2.0 flash, она сейчас самая стабильная
    contents="Здарова! 2+2?"
)

print(response.text)