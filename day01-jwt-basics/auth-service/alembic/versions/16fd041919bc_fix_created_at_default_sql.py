"""fix created_at default sql

Revision ID: 16fd041919bc
Revises: 1ce97a6ee7b2
Create Date: 2026-02-11 14:22:48.743360

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '16fd041919bc'
down_revision: Union[str, Sequence[str], None] = '1ce97a6ee7b2'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.execute("ALTER TABLE users ALTER COLUMN created_at SET DEFAULT now()")



def downgrade() -> None:
    """Downgrade schema."""
    pass
