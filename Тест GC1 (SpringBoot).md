Приложение: Spring Boot backend в Docker контейнере.  
 Инструменты наблюдения: Java Mission Control (JMC), GC logs, JMX/JFR.

Базовый уровень памяти после GC: примерно 52–54 MB.  
 В покое нижняя граница не росла — baseline был стабильным.

Тестил с помощью волновых нагрузок

for r in {1..5}; do    
  for n in 1 2 3 4; do    
    (    
      for i in {1..200}; do    
        curl \-s "http://localhost:8080/api/v1/goals?page=0\&size=20" \\    
          \-H "Authorization: Bearer $ACCESS\_TOKEN" \> /dev/null    
      done    
    ) &    
  done    
  wait    
  sleep 20    
done

Исходя из логов:  
 app-1 | \[2026-04-23T11:29:42.431+0000\]\[16149.742s\]\[info \]\[gc \] GC(422) Pause Young (Normal) (G1 Evacuation Pause) 125M-\>51M(134M) 9.935ms

обычно обьем памяти циклично возрастал примерно до 125мб, после чистки возращался к значениям 50-52мб

| GC ID | Cause | Collector | Longest Pause (ticks) | Duration (ticks) | Used Heap Before | Used Heap After | Used Heap Delta |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| **372** | Heap Inspection Initiated | G1Full | 35 621 000 | 35 621 000 | 65.5 MB | 52.0 MB | \-13.5 MB |
| **373** | G1 Evacuation Pause | G1New | 1 423 333 | 1 423 333 | 130.7 MB | 52.3 MB | \-78.4 MB |
| **374** | G1 Evacuation Pause | G1Old | 4 282 084 | 40 402 917 | 52.3 MB | 54.4 MB | \+2.1 MB |
| **375** | G1 Evacuation Pause | G1New | 2 337 750 | 2 337 750 | 128.9 MB | 52.3 MB | \-76.6 MB |
| **376** | G1 Evacuation Pause | G1New | 2 335 292 | 2 335 292 | 128.8 MB | 52.5 MB | \-76.3 MB |
| **377** | G1 Evacuation Pause | G1New | 1 633 083 | 1 633 083 | 129.0 MB | 52.5 MB | \-76.6 MB |
| **378** | G1 Evacuation Pause | G1Old | 2 737 125 | 39 241 375 | 52.5 MB | 54.6 MB | \+2.1 MB |
| **379** | G1 Evacuation Pause | G1New | 2 367 250 | 2 367 250 | 129.0 MB | 52.6 MB | \-76.4 MB |
| **380** | G1 Evacuation Pause | G1New | 2 938 416 | 2 938 416 | 129.2 MB | 52.5 MB | \-76.6 MB |
| **381** | G1 Evacuation Pause | G1New | 1 915 125 | 1 915 125 | 130.1 MB | 52.7 MB | \-77.5 MB |
| **382** | G1 Evacuation Pause | G1Old | 3 463 583 | 43 622 375 | 52.7 MB | 54.8 MB | \+2.1 MB |
| **383** | G1 Evacuation Pause | G1New | 2 033 625 | 2 033 625 | 130.3 MB | 52.7 MB | \-77.6 MB |
| **384** | G1 Evacuation Pause | G1New | 5 398 541 | 5 398 541 | 130.3 MB | 52.6 MB | \-77.7 MB |
| **385** | G1 Evacuation Pause | G1Old | 2 981 208 | 57 905 000 | 52.6 MB | 52.6 MB | 0 B |
| **386** | G1 Evacuation Pause | G1New | 1 472 417 | 1 472 417 | 130.2 MB | 52.7 MB | \-77.5 MB |
| **387** | G1 Evacuation Pause | G1New | 2 037 250 | 2 037 250 | 131.3 MB | 52.8 MB | \-78.5 MB |
| **388** | G1 Evacuation Pause | G1Old | 2 970 750 | 39 939 125 | 52.8 MB | 54.9 MB | \+2.1 MB |
| **389** | G1 Evacuation Pause | G1New | 4 683 917 | 4 683 917 | 130.4 MB | 52.7 MB | \-77.7 MB |
| **390** | G1 Evacuation Pause | G1New | 6 116 000 | 6 116 000 | 130.3 MB | 52.9 MB | \-77.5 MB |
| **391** | G1 Evacuation Pause | G1Old | 3 493 625 | 52 242 750 | 52.9 MB | 52.9 MB | 0 B |
| **392** | G1 Evacuation Pause | G1New | 4 503 333 | 4 503 333 | 130.5 MB | 52.8 MB | \-77.6 MB |

Таблица  саммых частых классов которые использовались

| Class | Alloc in TLABs (MB) | Outside TLAB (MB) | Outside TLAB (%) |
| :---- | :---- | :---- | :---- |
| **byte\[\]** | 265.8 MB | 23.4 MB | 41.2 % |
| **java.lang.Object\[\]** | 114.3 MB | 4.3 MB | 7.5 % |
| **char\[\]** | 63.8 MB | 24.0 MB | 42.3 % |
| **int\[\]** | 41.8 MB | 4.1 MB | 7.3 % |
| **long\[\]** | 31.0 MB | 0 MB | 0 % |

самые частые классы внутри моего проекта ( которые чаще всего используются в аллокации)

| Class (упрощенно) | Alloc in TLABs (KB) | Alloc (%) | Доп. инфо |
| :---- | :---- | :---- | :---- |
| **ReaderBasedJsonParser** | 2154.6 KB | 0.21% | Парсинг JSON |
| **JsonReadContext** | 1129.5 KB | 0.11% | Контекст чтения |
| **DupDetector** | 1106.7 KB | 0.11% | Проверка дубликатов |
| **UntypedObjectDeserializer** | 561.4 KB | 0.05% | Десериализация |
| **HmacSHA384** | 489.8 KB | 0.05% | Криптография |
| **CharsToNameCanonicalizer** | 481.4 KB | 0.05% | Работа со строками |
| **GcInfo / GcInfoCompositeData** | 262.2 KB | 0.02% | Логирование GC |

то как ведет себя old generation 

| Event ID | Old Regions Change | Результат / Интерпретация |
| :---- | :---- | :---- |
| **GC(312)** | 50 → 50 | **Стабильность:** Old не вырос, объекты не перемещались. |
| **GC(313)** | 50 → 49 | **Эффективность:** Mixed GC успешно очистил часть данных в Old. |
| **GC(301)** | 50 → 51 | **Promotion:** Небольшой объем данных перешел из Young в Old. |
| **GC(286)** | 50 → 51 | **Promotion:** Повторный переход выживших объектов в Old. |
| **GC(347)** | 50 → 50 | **Стабильность:** Объем старого поколения остается неизменным. |

