import json


class Meal:
    def __init__(self, id: str = "", title: str = "", cost: int = 0):
        self.id = id
        self.title = title
        self.cost = cost

    def get_id(self) -> str:
        return self.id

    def get_title(self) -> str:
        return self.title

    def get_cost(self) -> int:
        return self.cost
    
    def to_dict(self):
        return{
            "id":self.id,
            "title":self.title,
            "cost":self.cost
               }

    def to_json(self) -> str:
        return json.dumps({
            "id": self.id,
            "title": self.title,
            "cost": self.cost
        }, ensure_ascii=False)

    def from_json(self, json_str: str):
        data = json.loads(json_str)
        self.id = data.get("id", "")
        self.title = data.get("title", "")
        self.cost = data.get("cost", 0)

    @staticmethod
    def from_dict(data: dict) -> 'Meal':
        return Meal(
            id=data.get("id", ""),
            title=data.get("title", ""),
            cost=data.get("cost", 0)
        )
