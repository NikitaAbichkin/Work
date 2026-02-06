from fastapi import FastAPI
from file_helper import FileHelper
import json
import uvicorn

app = FastAPI()

@app.get("/menu")
def get_menu():
    menu = FileHelper.get_string_from_file("Menu1.json")
    return json.loads(menu)

@app.post("/menu")
def add_meal(meal: dict):
    FileHelper.add_meal_to_menu(meal)
    return FileHelper.load_menu()

@app.delete("/menu/{meal_id}")
def delete(meal_id:str):
    FileHelper.delete_meal_from_menu(meal_id)
    return FileHelper.load_menu()

@app.put("/menu")
def update_meal_from_menu(Example:dict):
    FileHelper.update_meal_from_menu(Example)
    return FileHelper.load_menu()


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=4004)




