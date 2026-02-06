# Все операции со студентами в базе данных
from datetime import date, datetime
from database import Database
from student import Student


class StudentsDB:
    def __init__(self, db: Database):
        self.db = db

    # ── Создание таблицы ──

    def create_table(self):
        """Создаёт таблицу students если её ещё нет"""
        self.db.run("""
            CREATE TABLE IF NOT EXISTS students (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                name     TEXT NOT NULL,
                birthday TEXT NOT NULL,
                grp      TEXT NOT NULL
            )
        """)

    # ── CREATE — добавление ──

    def add(self, name: str, birthday: date, group: str) -> int:
        """Добавляет студента. Возвращает его ID."""
        cursor = self.db.run(
            "INSERT INTO students (name, birthday, grp) VALUES (?, ?, ?)",
            (name, birthday.strftime("%Y-%m-%d"), group)
        )
        return cursor.lastrowid

    # ── READ — чтение ──

    def get_by_id(self, student_id: int) -> Student | None:
        """Возвращает студента по ID (или None если не найден)"""
        row = self.db.fetch_one(
            "SELECT id, name, birthday, grp FROM students WHERE id = ?",
            (student_id,)
        )
        if row is None:
            return None
        return self._row_to_student(row)

    def get_all(self) -> list[Student]:
        """Возвращает список всех студентов"""
        rows = self.db.fetch_all("SELECT id, name, birthday, grp FROM students")
        return [self._row_to_student(r) for r in rows]

    def get_by_group(self, group: str) -> list[Student]:
        """Возвращает студентов из указанной группы"""
        rows = self.db.fetch_all(
            "SELECT id, name, birthday, grp FROM students WHERE grp = ?",
            (group,)
        )
        return [self._row_to_student(r) for r in rows]

    # ── UPDATE — обновление ──

    def update(self, student_id: int, name: str, birthday: date, group: str) -> bool:
        """Обновляет данные студента. Возвращает True если запись найдена."""
        cursor = self.db.run(
            "UPDATE students SET name = ?, birthday = ?, grp = ? WHERE id = ?",
            (name, birthday.strftime("%Y-%m-%d"), group, student_id)
        )
        return cursor.rowcount > 0

    # ── DELETE — удаление ──

    def delete(self, student_id: int) -> bool:
        """Удаляет студента по ID. Возвращает True если запись найдена."""
        cursor = self.db.run(
            "DELETE FROM students WHERE id = ?",
            (student_id,)
        )
        return cursor.rowcount > 0

    # ── Вспомогательное ──

    def _row_to_student(self, row: tuple) -> Student:
        """Преобразует строку из БД в объект Student"""
        return Student(
            id=row[0],
            name=row[1],
            birthday=datetime.strptime(row[2], "%Y-%m-%d").date(),
            group=row[3]
        )
