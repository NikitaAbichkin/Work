import json
from typing import List
from meal import Meal



class Order:
    def __init__(self, id: str = "", date: str = ""):
        self.id = id
        self.date = date
        self.meals: List[Meal] = []

    def add_meal(self, meal: Meal):
        self.meals.append(meal)

    def add_meals(self, meals: List[Meal]):
        self.meals.extend(meals)

    def get_id(self) -> str:
        return self.id

    def get_date(self) -> str:
        return self.date

    def get_meals(self) -> List[Meal]:
        return self.meals

    def set_id(self, id: str):
        self.id = id

    def to_json(self) -> str:
        meals_list = [{"id": m.id, "title": m.title, "cost": m.cost} for m in self.meals]
        return json.dumps({
            "StudentID": self.id,
            "data": self.date,
            "meals": meals_list
        }, ensure_ascii=False)
    
    def to_dict(self):
        meals_list =[]
        for i in self.meals:
            meals_list.append(i.to_dict())
        return {
            "StudentID": self.id,
            "data": self.date,
            "meals": meals_list 
            }
    
   
    
        

    def from_json(self, json_str: str):
        data = json.loads(json_str)
        self.id = data.get("StudentID", "")
        self.date = data.get("data", "")
        meals_data = data.get("meals", [])
        self.meals = []
        if isinstance(meals_data, list):
            for meal_dict in meals_data:
                self.meals.append(Meal.from_dict(meal_dict))

    @staticmethod
    def from_dict(data: dict) -> 'Order':
        order = Order(
            id=data.get("StudentID", ""),
            date=data.get("data", "")
        )
        meals_data = data.get("meals", [])
        if isinstance(meals_data, list):
            for meal_dict in meals_data:
                order.add_meal(Meal.from_dict(meal_dict))
        return order
