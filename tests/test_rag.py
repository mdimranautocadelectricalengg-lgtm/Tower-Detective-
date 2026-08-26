from ai.rag import SimpleRAG


def test_rag():

    rag = SimpleRAG()

    results = rag.search(
        "LTE signal dBm",
        top_k=2
    )

    assert len(results) > 0
