from dataclasses import dataclass


@dataclass
class Order:
    quantity: int
    symbol: str
