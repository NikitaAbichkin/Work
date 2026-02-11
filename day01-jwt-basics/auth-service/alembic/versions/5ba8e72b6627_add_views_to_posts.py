"""add views to posts

Revision ID: 5ba8e72b6627
Revises: 9a55abf62f22
Create Date: 2026-02-11 19:53:30.886915

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '5ba8e72b6627'
down_revision: Union[str, Sequence[str], None] = '9a55abf62f22'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.add_column("posts",sa.Column("views",sa.Integer(),server_default="0",nullable=False))
    pass


def downgrade() -> None:
    op.drop_column("posts","views")
    pass
