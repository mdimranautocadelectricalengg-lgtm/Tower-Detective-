from ai.tools import (
    signal_interpretation,
    find_serving_cell,
    compare_cells
)


def test_signal():

    assert signal_interpretation(-70) == \
        "Generally strong"


def test_serving_cell():

    cells = [
        {
            "cellId": 123,
            "registered": False
        },
        {
            "cellId": 456,
            "registered": True
        }
    ]

    result = find_serving_cell(
        cells
    )

    assert result["cellId"] == 456


def test_cell_change():

    previous = {
        "cellId": 100
    }

    current = {
        "cellId": 200
    }

    result = compare_cells(
        previous,
        current
    )

    assert result["changed"] is True
