# Модель студента — просто хранит данные
from datetime import date


class Student:
    def __init__(self, id: int, name: str, birthday: date, group: str):
        self.id = id            # Уникальный номер в базе
        self.name = name        # ФИО
        self.birthday = birthday  # Дата рождения
        self.group = group      # Группа (например "ИС-21")

    def __str__(self) -> str:
        """Вызывается при print(student). Удобнее чем отдельный метод to_string()"""
        return f"[{self.id}] {self.name} | {self.birthday} | {self.group}"
