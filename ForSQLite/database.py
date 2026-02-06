# Простой класс для работы с SQLite
import sqlite3


class Database:
    def __init__(self, filename: str):
        """Принимает имя файла БД, например 'students.db'"""
        self.filename = filename

    def connect(self) -> sqlite3.Connection:
        """Создаёт и возвращает соединение с базой данных"""
        return sqlite3.connect(self.filename)

    def run(self, sql: str, params: tuple = ()) -> sqlite3.Cursor:
        """Выполняет SQL-запрос и сохраняет изменения.

        sql    — строка SQL-запроса, например 'INSERT INTO ...'
        params — значения для подстановки вместо ?

        Возвращает cursor для чтения результатов.
        """
        conn = self.connect()
        cursor = conn.cursor()
        cursor.execute(sql, params)
        conn.commit()
        conn.close()
        return cursor

    def fetch_one(self, sql: str, params: tuple = ()):
        """Выполняет SELECT и возвращает одну строку (или None)"""
        conn = self.connect()
        cursor = conn.cursor()
        cursor.execute(sql, params)
        row = cursor.fetchone()
        conn.close()
        return row

    def fetch_all(self, sql: str, params: tuple = ()) -> list:
        """Выполняет SELECT и возвращает все строки"""
        conn = self.connect()
        cursor = conn.cursor()
        cursor.execute(sql, params)
        rows = cursor.fetchall()
        conn.close()
        return rows
