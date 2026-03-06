( c индексом )
'Seq Scan on orders  (cost=0.00..5661.00 rows=153235 width=22) (actual time=0.023..27.140 rows=152866 loops=1)', '  Filter: (cost > ''500''::numeric)', '  Rows Removed by Filter: 147134', 'Planning Time: 0.178 ms', 'Execution Time: 31.313 ms'

( без индекса )
'Seq Scan on orders  (cost=0.00..5661.00 rows=153235 width=22) (actual time=0.015..26.690 rows=152866 loops=1)', '  Filter: (cost > ''500''::numeric)', '  Rows Removed by Filter: 147134', 'Planning Time: 0.285 ms', 'Execution Time: 30.872 ms'

( до написания составного индекса )
'Gather  (cost=1000.00..5558.26 rows=2 width=22) (actual time=4.535..19.447 rows=1 loops=1)', '  Workers Planned: 1', '  Workers Launched: 1', '  ->  Parallel Seq Scan on orders  (cost=0.00..4558.06 rows=1 width=22) (actual time=8.894..15.802 rows=0 loops=2)', '        Filter: ((cost > ''500''::numeric) AND (user_id = 150))', '        Rows Removed by Filter: 150000', 'Planning Time: 0.267 ms', 'Execution Time: 19.534 ms'

( после написания составного индекса )
'Bitmap Heap Scan on orders  (cost=4.44..12.28 rows=2 width=22) (actual time=0.095..0.096 rows=1 loops=1)', '  Recheck Cond: ((user_id = 150) AND (cost > ''500''::numeric))', '  Heap Blocks: exact=1', '  ->  Bitmap Index Scan on idx_orders_user_cost  (cost=0.00..4.44 rows=2 width=0) (actual time=0.085..0.085 rows=1 loops=1)', '        Index Cond: ((user_id = 150) AND (cost > ''500''::numeric))', 'Planning Time: 0.493 ms', 'Execution Time: 0.126 ms'