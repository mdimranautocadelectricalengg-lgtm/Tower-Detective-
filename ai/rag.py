from pathlib import Path

import numpy as np

from sentence_transformers import SentenceTransformer


KNOWLEDGE_DIR = Path("knowledge")


class SimpleRAG:

    def __init__(self):

        self.documents = []

        self.model = SentenceTransformer(
            "sentence-transformers/all-MiniLM-L6-v2"
        )

        self.load_documents()


    def load_documents(self):

        for file in KNOWLEDGE_DIR.rglob("*.md"):

            text = file.read_text(
                encoding="utf-8"
            )

            self.documents.append({
                "file": str(file),
                "text": text
            })


    def search(
        self,
        query,
        top_k=3
    ):

        if not self.documents:

            return []


        texts = [
            item["text"]
            for item in self.documents
        ]


        query_vector = self.model.encode(
            [query],
            normalize_embeddings=True
        )[0]


        document_vectors = self.model.encode(
            texts,
            normalize_embeddings=True
        )


        scores = np.dot(
            document_vectors,
            query_vector
        )


        indexes = np.argsort(
            scores
        )[::-1][:top_k]


        results = []


        for index in indexes:

            results.append({

                "file":
                    self.documents[index]["file"],

                "text":
                    self.documents[index]["text"],

                "score":
                    float(scores[index])
            })


        return results
