# -*- coding: utf-8 -*-
import gzip, json, sys
total = 0
for name in ["hafez", "khayyam", "saadi", "rumi", "stories"]:
    p = f"app/src/main/assets/corpus/{name}.json.gz"
    d = json.loads(gzip.open(p, "rt", encoding="utf-8").read())
    ids = [x["id"] for x in d]
    assert len(ids) == len(set(ids)), f"{name}: duplicate ids"
    for x in d:
        assert x.get("verses"), f"{name} id {x['id']}: empty verses"
        assert x.get("tafsir", "").strip(), f"{name} id {x['id']}: empty tafsir"
        assert x.get("themeTag"), f"{name} id {x['id']}: empty themeTag"
    total += len(d)
print(f"corpus OK: {total} poems")
