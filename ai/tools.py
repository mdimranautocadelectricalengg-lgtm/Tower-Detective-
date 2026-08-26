def signal_interpretation(
    signal_dbm
):

    if signal_dbm is None:

        return "Unavailable"


    signal_dbm = float(
        signal_dbm
    )


    if signal_dbm >= -80:

        return "Generally strong"


    if signal_dbm >= -95:

        return "Moderate"


    if signal_dbm >= -110:

        return "Weak"


    return "Very weak"


def find_serving_cell(
    cells
):

    for cell in cells:

        if cell.get(
            "registered"
        ):

            return cell


    return None


def compare_cells(
    previous,
    current
):

    if not previous or not current:

        return {
            "changed": False,
            "reason": "Insufficient data"
        }


    old_id = previous.get(
        "cellId"
    )

    new_id = current.get(
        "cellId"
    )


    return {

        "changed":
            old_id != new_id,

        "previous":
            old_id,

        "current":
            new_id
    }
