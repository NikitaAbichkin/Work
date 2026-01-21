import json
from typing import List, Type, TypeVar
from meal import Meal
from order import Order

T = TypeVar('T', Meal, Order)


class FileHelper:
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
    def save_jsonable(filename: str, obj) -> int:
        if obj is None:
            return 1
        return FileHelper.write_to_file(filename, obj.to_json())

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
