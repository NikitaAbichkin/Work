import json
from typing import List, Type, TypeVar
from meal import Meal
from order import Order

T = TypeVar('T', Meal, Order)


class FileHelper:
    @staticmethod
    def save_list_to_file(filename,items:list):
        try:
            dict_list = []
            for i in items:
                i:Order
                dict_list.append(i.to_dict())
            json_string = json.dumps(dict_list,ensure_ascii=False)
            FileHelper.write_to_file(filename,json_string)
        except:
            return None
        
                

    @staticmethod
    def load_menu():
        content  = FileHelper.get_string_from_file("Menu1.json")
        return json.loads(content)
    @staticmethod
    def  save_menu(menu:list):
        string_menu = json.dumps(menu,ensure_ascii=False)
        FileHelper.write_to_file("Menu1.json",string_menu)






    @staticmethod
    def write_to_file(filename: str, data: str) -> int:
        try:
            with open(filename, 'w', encoding='utf-8') as f:
                f.write(data)
            return 0
        except FileNotFoundError:
            return 1

    @staticmethod
    def get_string_from_file(filename: str) -> str:
        try:
            with open(filename, 'r', encoding='utf-8') as f:
                return f.read()
        except Exception as e:
            return str(e)
    
    @staticmethod
    def delete_meal_from_menu(id:str):
        menu = FileHelper.get_string_from_file("Menu1.json")
        menu = json.loads(menu)
        for i in menu:
            if i["id"]==id:
                menu.remove(i)
        json_string = json.dumps(menu,ensure_ascii=False)
        FileHelper.write_to_file("Menu1.json",json_string)

    @staticmethod
    def update_meal_from_menu(Example:dict):
        menu = FileHelper.load_menu()
        for i in menu:
            if i["id"] == Example["id"]:
                i["title"] = Example["title"]
                i["cost"] = Example["cost"]
                break
        FileHelper.save_menu(menu)
    
    @staticmethod
    def add_meal_to_menu(Example:dict):
        menu:list  = FileHelper.load_menu()
        menu.append(Example)
        FileHelper.save_menu(menu)


    @staticmethod
    def universal_code_for_parsing(content: str, clazz: Type[T]) -> List[T]:
        result_list = []
        try:
            data = json.loads(content)
            if isinstance(data, list):
                for item in data:
                    if clazz == Meal:
                        result_list.append(Meal.from_dict(item))
                    elif clazz == Order:
                        result_list.append(Order.from_dict(item))
        except json.JSONDecodeError:
            pass
        return result_list
