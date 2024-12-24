"""
This module contains SQLite triggers that emulate stored procedures for sales statistics.
These triggers automatically update the statistics tables whenever sales or refunds occur.
"""

from sqlalchemy import event, text
from datetime import datetime, UTC
from . import models

def setup_stored_procedures(Base, engine):
    """Set up all stored procedure triggers"""
    
    @event.listens_for(models.TicketSale, 'after_insert')
    def update_stats_after_sale(mapper, connection, target):
        """Update all statistics tables after a new sale"""
        if not target.status:  # Skip if sale is already marked as refunded
            return
            
        sale_date = target.sale_time.date().isoformat()
        year_month = target.sale_time.strftime("%Y-%m")
        
        # Update daily stats
        stmt = text("""
        INSERT INTO daily_sales_stats (date, total_transactions, total_quantity, total_amount, updated_at)
        VALUES (:date, 1, :quantity, :amount, :updated_at)
        ON CONFLICT(date) DO UPDATE SET
            total_transactions = total_transactions + 1,
            total_quantity = total_quantity + :quantity,
            total_amount = total_amount + :amount,
            updated_at = :updated_at
        """)
        connection.execute(stmt, {
            'date': sale_date,
            'quantity': target.quantity,
            'amount': target.total_amount,
            'updated_at': datetime.now(UTC)
        })
        
        # Update monthly stats
        stmt = text("""
        INSERT INTO monthly_sales_stats (year_month, total_transactions, total_quantity, total_amount, updated_at)
        VALUES (:year_month, 1, :quantity, :amount, :updated_at)
        ON CONFLICT(year_month) DO UPDATE SET
            total_transactions = total_transactions + 1,
            total_quantity = total_quantity + :quantity,
            total_amount = total_amount + :amount,
            updated_at = :updated_at
        """)
        connection.execute(stmt, {
            'year_month': year_month,
            'quantity': target.quantity,
            'amount': target.total_amount,
            'updated_at': datetime.now(UTC)
        })
        
        # Update ticket type stats
        stmt = text("""
        INSERT INTO ticket_type_stats (ticket_type_id, total_transactions, total_quantity, total_amount, updated_at)
        VALUES (:ticket_type_id, 1, :quantity, :amount, :updated_at)
        ON CONFLICT(ticket_type_id) DO UPDATE SET
            total_transactions = total_transactions + 1,
            total_quantity = total_quantity + :quantity,
            total_amount = total_amount + :amount,
            updated_at = :updated_at
        """)
        connection.execute(stmt, {
            'date': sale_date,
            'ticket_type_id': target.ticket_type_id,
            'quantity': target.quantity,
            'amount': target.total_amount,
            'updated_at': datetime.now(UTC)
        })
        
        # Update operator stats
        stmt = text("""
        INSERT INTO operator_stats (operator_id, total_transactions, total_quantity, total_amount, updated_at)
        VALUES (:operator_id, 1, :quantity, :amount, :updated_at)
        ON CONFLICT(operator_id) DO UPDATE SET
            total_transactions = total_transactions + 1,
            total_quantity = total_quantity + :quantity,
            total_amount = total_amount + :amount,
            updated_at = :updated_at
        """)
        connection.execute(stmt, {
            'date': sale_date,
            'operator_id': target.operator_id,
            'quantity': target.quantity,
            'amount': target.total_amount,
            'updated_at': datetime.now(UTC)
        })

    @event.listens_for(models.Refund, 'after_insert')
    def update_stats_after_refund(mapper, connection, target):
        """Update all statistics tables after a refund"""
        # Get the associated sale
        sale = connection.execute(
            text("SELECT * FROM ticket_sales WHERE id = :sale_id"),
            {'sale_id': target.sale_id}
        ).first()
        
        if not sale:
            return
            
        sale_date = datetime.fromisoformat(sale.sale_time).date().isoformat()
        year_month = datetime.fromisoformat(sale.sale_time).strftime("%Y-%m")
        
        # Update daily stats
        stmt = text("""
        UPDATE daily_sales_stats
        SET total_transactions = total_transactions - 1,
            total_quantity = total_quantity - :quantity,
            total_amount = total_amount - :amount,
            updated_at = :updated_at
        WHERE date = :date
        """)
        connection.execute(stmt, {
            'date': sale_date,
            'quantity': sale.quantity,
            'amount': sale.total_amount,
            'updated_at': datetime.now(UTC)
        })
        
        # Update monthly stats
        stmt = text("""
        UPDATE monthly_sales_stats
        SET total_transactions = total_transactions - 1,
            total_quantity = total_quantity - :quantity,
            total_amount = total_amount - :amount,
            updated_at = :updated_at
        WHERE year_month = :year_month
        """)
        connection.execute(stmt, {
            'year_month': year_month,
            'quantity': sale.quantity,
            'amount': sale.total_amount,
            'updated_at': datetime.now(UTC)
        })
        
        # Update ticket type stats
        stmt = text("""
        UPDATE ticket_type_stats
        SET total_transactions = total_transactions - 1,
            total_quantity = total_quantity - :quantity,
            total_amount = total_amount - :amount,
            updated_at = :updated_at
        WHERE ticket_type_id = :ticket_type_id
        """)
        connection.execute(stmt, {
            'date': sale_date,
            'ticket_type_id': sale.ticket_type_id,
            'quantity': sale.quantity,
            'amount': sale.total_amount,
            'updated_at': datetime.now(UTC)
        })
        
        # Update operator stats
        stmt = text("""
        UPDATE operator_stats
        SET total_transactions = total_transactions - 1,
            total_quantity = total_quantity - :quantity,
            total_amount = total_amount - :amount,
            updated_at = :updated_at
        WHERE operator_id = :operator_id
        """)
        connection.execute(stmt, {
            'date': sale_date,
            'operator_id': sale.operator_id,
            'quantity': sale.quantity,
            'amount': sale.total_amount,
            'updated_at': datetime.now(UTC)
        })
