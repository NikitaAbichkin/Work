"""extend username length to 100

Revision ID: 35c28e24afc7
Revises: 16fd041919bc
Create Date: 2026-02-11 18:38:22.262385

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '35c28e24afc7'
down_revision: Union[str, Sequence[str], None] = '16fd041919bc'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.alter_column (
        "users","username",
        type_=sa.String(length=100),
        existing_type=sa.String(length=50)
    )
    pass


def downgrade() -> None:
    op.alter_column('users', 'username',
        type_=sa.String(length=50),
        existing_type=sa.String(length=100)
        )
    pass
