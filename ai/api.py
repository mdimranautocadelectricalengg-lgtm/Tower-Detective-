from fastapi import FastAPI
from pydantic import BaseModel

from rag import SimpleRAG
from tools import (
    signal_interpretation,
    find_serving_cell
)


app = FastAPI(
    title="Tower Detective AI"
)


rag = SimpleRAG()


class AnalyzeRequest(BaseModel):

    networkType: str | None = None

    operator: str | None = None

    cells: list = []

    latitude: float | None = None

    longitude: float | None = None

    accuracy: float | None = None


@app.get("/")
def home():

    return {
        "name": "Tower Detective AI",
        "status": "running"
    }


@app.post("/analyze")
def analyze(
    request: AnalyzeRequest
):

    serving = find_serving_cell(
        request.cells
    )


    signal = (
        serving.get("signalDbm")
        if serving
        else None
    )


    signal_description = (
        signal_interpretation(
            signal
        )
    )


    knowledge = rag.search(
        "cellular signal dBm "
        "serving cell network technology",
        top_k=3
    )


    return {

        "network":
            request.networkType,

        "operator":
            request.operator,

        "servingCell":
            serving,

        "signal":
            signal,

        "signalDescription":
            signal_description,

        "gps": {

            "latitude":
                request.latitude,

            "longitude":
                request.longitude,

            "accuracy":
                request.accuracy
        },

        "knowledge":
            knowledge
    }
