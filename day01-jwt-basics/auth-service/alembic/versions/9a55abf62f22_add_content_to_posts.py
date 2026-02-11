""" add content to posts

Revision ID: 9a55abf62f22
Revises: b1f029e58a43
Create Date: 2026-02-11 19:09:59.522614

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '9a55abf62f22'
down_revision: Union[str, Sequence[str], None] = 'b1f029e58a43'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.add_column("posts", sa.Column("context",sa.String(500)))
    pass


def downgrade() -> None:
    op.drop_column("posts","context")
    pass
