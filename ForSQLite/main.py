# Точка входа — тут запускаем и тестируем всё
from datetime import date
from database import Database
from students_db import StudentsDB

def main():
    # 1. Подключаемся к базе данных (файл создастся автоматически)
    db = Database("students.db")

    # 2. Создаём таблицу студентов
    students = StudentsDB(db) #класс обьект класса students
    students.create_table()

    # 3. Добавляем студентов
    id1 = students.add("Иван Иванов", date(2005, 3, 15), "ИС-21")
    id2 = students.add("Мария Петрова", date(2004, 7, 22), "ИС-21")
    id3 = students.add("Алексей Сидоров", date(2005, 1, 10), "ИС-22")

    print("--- Все студенты ---")
    for s in students.get_all():
        print(s)

    # 4. Получаем одного студента по ID
    print(f"\n--- Студент с ID {id2} ---")
    print(students.get_by_id(id2))

    # 5. Обновляем группу студента
    students.update(id1, name="Иван Иванов", birthday=date(2005, 3, 15), group="ИС-22")
    print(f"\n--- После перевода Ивана в ИС-22 ---")
    for s in students.get_by_group("ИС-22"):
        print(s)

    # 6. Удаляем студента
    students.delete(id3)
    print(f"\n--- После удаления студента {id3} ---")
    for s in students.get_all():
        print(s)

if __name__ == "__main__":
    main()
