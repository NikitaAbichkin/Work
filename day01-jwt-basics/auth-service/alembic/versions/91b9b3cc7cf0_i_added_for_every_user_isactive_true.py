"""i added for every user isActive = True

Revision ID: 91b9b3cc7cf0
Revises: 83b2db87eb2d
Create Date: 2026-02-11 13:42:55.958085

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '91b9b3cc7cf0'
down_revision: Union[str, Sequence[str], None] = '83b2db87eb2d'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.execute("UPDATE users SET is_active = TRUE WHERE is_active IS NULL")
    pass


def downgrade() -> None:

    pass
