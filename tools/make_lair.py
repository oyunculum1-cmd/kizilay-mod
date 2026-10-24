"""Kızıl Ay Göksarayı (gökyüzü yapısı) üretici -> src/main/resources/data/kizilay/structures/lair/arena.nbt
Çalıştır: python3 tools/make_lair.py   (numpy + gzip yeterli)"""
import math, random, struct, gzip, sys, os
import numpy as np

rnd = random.Random(1337)
W = D = 65
H = 64
CX = CZ = 32
F = 18  # arena zemin seviyesi
B = {}  # (x,y,z) -> (name, props tuple)


def put(x, y, z, name, **props):
    x, y, z = int(x), int(y), int(z)
    if 0 <= x < W and 0 <= y < H and 0 <= z < D:
        B[(x, y, z)] = (name, tuple(sorted(props.items())))


def has(x, y, z):
    return (x, y, z) in B


def n2(x, z, s=1.0):
    return (math.sin(x * 0.31 * s + 1.3) * math.cos(z * 0.27 * s) + math.sin((x + z) * 0.19 * s + 2.0)
            + 0.5 * math.sin(x * 0.7 * s) * math.sin(z * 0.63 * s + 0.4)) / 2.5


def wool(y, ybot, top):
    if y >= top - 1:
        return 'minecraft:white_wool'
    k = rnd.random()
    low = (y - ybot) < 3
    if low and k < 0.55:
        return 'minecraft:light_gray_wool'
    if low and k < 0.75:
        return 'minecraft:gray_wool'
    if k < 0.12:
        return 'minecraft:calcite'
    if k < 0.28:
        return 'minecraft:light_gray_wool'
    return 'minecraft:white_wool'


# ---- bulut kütlesi + alt sivri kaya ----
TOP = F - 1
for x in range(W):
    for z in range(D):
        dx, dz = x - CX, z - CZ
        d = math.hypot(dx, dz)
        th = math.atan2(dz, dx)
        R = 28.5 + 2.2 * math.sin(5 * th + 0.7) + 1.4 * math.sin(9 * th + 1.0) + 1.0 * math.sin(14 * th)
        if d > R:
            continue
        t = max(0.0, 1 - d / R)
        depth = int(round(1 + (t ** 0.55) * 11 + n2(x, z) * 2.2))
        ybot = max(3, TOP - depth)
        for y in range(ybot, TOP + 1):
            put(x, y, z, wool(y, ybot, TOP))
# alt kaya sarkıt (ters kule)
for x in range(W):
    for z in range(D):
        d = math.hypot(x - CX, z - CZ)
        if d > 9:
            continue
        L = int(TOP - 1 - (1 - d / 9) ** 1.3 * 15 + n2(x, z, 2) * 1.2)
        ytip = max(0, TOP - 16 + int(d * 1.2 + n2(x, z, 1.7) * 2))
        for y in range(ytip, TOP + 1):
            k = rnd.random()
            nm = 'minecraft:blackstone'
            if k < 0.15:
                nm = 'minecraft:basalt'
            elif k < 0.22:
                nm = 'minecraft:magma_block'
            elif k < 0.26:
                nm = 'minecraft:gilded_blackstone'
            elif k < 0.30:
                nm = 'minecraft:crying_obsidian'
            put(x, y, z, nm)

# ---- arena zemini ----
for x in range(W):
    for z in range(D):
        dx, dz = x - CX, z - CZ
        d = math.hypot(dx, dz)
        if d > 25.5 or not has(x, TOP, z):
            continue
        if d <= 23.2:
            nm = 'minecraft:polished_blackstone_bricks' if (x + z) % 2 == 0 else 'minecraft:polished_blackstone'
            if rnd.random() < 0.08:
                nm = 'minecraft:cracked_polished_blackstone_bricks'
            if 21.0 <= d < 22.4:
                nm = 'minecraft:gilded_blackstone' if (int(d * 3 + math.atan2(dz, dx) * 8) % 3 == 0) else 'minecraft:crying_obsidian'
            elif 14.6 <= d < 15.6:
                nm = 'minecraft:red_nether_bricks'
            elif 15.6 <= d < 16.2:
                nm = 'minecraft:gold_block'
            # ay hilali nakışı: büyük daire - kaydırılmış daire
            if d < 12:
                c1 = math.hypot(dx, dz)
                c2 = math.hypot(dx - 4.2, dz)
                if c1 < 11 and c2 > 9.2:
                    nm = 'minecraft:red_concrete'
                    if c1 > 10.1 or c2 < 10.3:
                        nm = 'minecraft:gold_block'
            # çatlak magma ışınları
            th = math.atan2(dz, dx)
            for k in range(8):
                a = k * math.pi / 4 + 0.2
                ex = abs(math.sin(th - a)) * d
                if 4.5 < d < 21 and ex < 0.55 and ((int(d) * 7 + k * 3) % 5 != 0) and not (d < 12 and nm in ('minecraft:red_concrete', 'minecraft:gold_block')):
                    nm = 'minecraft:magma_block'
            put(x, F, z, nm)
        elif d <= 25.2:
            put(x, F, z, 'minecraft:polished_blackstone_brick_slab', type='bottom')
# merkez kaide
for x in range(CX - 4, CX + 5):
    for z in range(CZ - 4, CZ + 5):
        d = math.hypot(x - CX, z - CZ)
        if d <= 3.6:
            put(x, F + 1, z, 'minecraft:polished_blackstone_bricks' if d > 2.2 else 'minecraft:gold_block')
        if d <= 2.2:
            put(x, F, z, 'minecraft:gold_block')
put(CX, F + 2, CZ, 'kizilay:moon_altar')
# kaide köşe fenerleri
for sx, sz in ((-3, -3), (3, -3), (-3, 3), (3, 3)):
    put(CX + sx, F + 2, CZ + sz, 'minecraft:shroomlight')
for sx, sz in ((-2, 0), (2, 0), (0, -2), (0, 2)):
    pass

# ---- sütunlar ----
def crescent2d(cx, cy, r1, r2, off, thick, plane='z', z0=0, name='minecraft:red_concrete', edge='minecraft:gold_block'):
    pts = set()
    for u in range(-int(r1) - 2, int(r1) + 3):
        for v in range(-int(r1) - 2, int(r1) + 3):
            if math.hypot(u, v) <= r1 and math.hypot(u - off, v) > r2:
                pts.add((u, v))
    for (u, v) in pts:
        edge_c = any((u + a, v + b) not in pts for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        for t in range(thick):
            put(cx + u, cy + v, z0 + t, edge if (edge_c or t in (0, thick - 1) and False) else name)


for k in range(12):
    a = k * math.tau / 12 + math.pi / 12
    px = CX + int(round(math.cos(a) * 20.5))
    pz = CZ + int(round(math.sin(a) * 20.5))
    h = 15 + (k % 2) * 3
    for dx in (-1, 0, 1):
        for dz in (-1, 0, 1):
            for y in range(F + 1, F + 1 + h):
                band = (y - F) % 5 == 0
                nm = 'minecraft:chiseled_polished_blackstone' if band else 'minecraft:polished_blackstone_bricks'
                if abs(dx) == 1 and abs(dz) == 1 and (y - F) % 5 in (1, 2):
                    nm = 'minecraft:gilded_blackstone'
                put(px + dx, y, pz + dz, nm)
    for dx in range(-2, 3):
        for dz in range(-2, 3):
            put(px + dx, F + h + 1, pz + dz, 'minecraft:gold_block' if max(abs(dx), abs(dz)) < 2 else 'minecraft:red_nether_bricks')
    # ışık
    for (dx, dz) in ((2, 0), (-2, 0), (0, 2), (0, -2)):
        put(px + dx, F + h - 3, pz + dz, 'minecraft:shroomlight')
    # tepe: ince hilal
    for dy in range(1, 5):
        put(px, F + h + 1 + dy, pz, 'minecraft:red_concrete' if dy < 4 else 'minecraft:gold_block')
    put(px + 1, F + h + 4, pz, 'minecraft:gold_block')
    put(px - 1, F + h + 4, pz, 'minecraft:gold_block')
    put(px, F + h + 6, pz, 'minecraft:shroomlight')

# ---- dev hilal + halka (arka plan) ----
MZ = 6
MC = (CX, 38)
crescent2d(MC[0], MC[1], 15.5, 12.4, 6.0, 3, z0=MZ)
for u in range(-26, 27):
    for v in range(-26, 27):
        d = math.hypot(u, v)
        if 18.2 <= d <= 19.6:
            ang = math.atan2(v, u)
            for t in range(3):
                nm = 'minecraft:gold_block' if int(ang * 10) % 3 else 'minecraft:red_stained_glass'
                if t != 1 and nm == 'minecraft:red_stained_glass':
                    nm = 'minecraft:gold_block'
                put(MC[0] + u, MC[1] + v, MZ + t, nm)
# halkayı bulutlara bağlayan ayak
for x in range(CX - 2, CX + 3):
    for y in range(F, F + 6):
        put(x, y, MZ + 1, 'minecraft:polished_blackstone_bricks')

# ---- uydu adacıkları ----
sats = [(10, 14, 34, 4), (54, 12, 40, 3), (6, 40, 44, 4), (58, 42, 36, 4), (20, 56, 48, 3), (46, 56, 42, 4), (32, 58, 30, 3)]
for (sx, sz, sy, sr) in sats:
    for x in range(sx - sr - 2, sx + sr + 3):
        for z in range(sz - sr - 2, sz + sr + 3):
            d = math.hypot(x - sx, z - sz)
            if d > sr + 0.5:
                continue
            depth = int(1 + (1 - d / (sr + 1)) * (sr + 3))
            for y in range(sy - depth, sy + 1):
                if y == sy:
                    put(x, y, z, 'minecraft:white_wool')
                elif y > sy - 2:
                    put(x, y, z, 'minecraft:light_gray_wool')
                else:
                    put(x, y, z, 'minecraft:blackstone' if rnd.random() < 0.8 else 'minecraft:magma_block')
    for dy in range(1, 7):
        put(sx, sy + dy, sz, 'minecraft:polished_blackstone_bricks')
    put(sx, sy + 7, sz, 'minecraft:gold_block')
    put(sx + 1, sy + 6, sz, 'minecraft:red_concrete')
    put(sx - 1, sy + 6, sz, 'minecraft:red_concrete')
    put(sx, sy + 8, sz, 'minecraft:shroomlight')

# ---- bulut tomurcukları (kenar) ----
for _ in range(70):
    a = rnd.random() * math.tau
    r = rnd.uniform(24.5, 30)
    px, pz = CX + math.cos(a) * r, CZ + math.sin(a) * r
    rad = rnd.uniform(2, 4.2)
    py = TOP + rnd.randint(0, 2)
    for x in range(int(px - rad - 1), int(px + rad + 2)):
        for z in range(int(pz - rad - 1), int(pz + rad + 2)):
            for y in range(py - 2, py + int(rad) + 1):
                if (x - px) ** 2 + ((y - py) * 1.5) ** 2 + (z - pz) ** 2 <= rad * rad and math.hypot(x - CX, z - CZ) > 24.5:
                    put(x, y, z, 'minecraft:white_wool' if rnd.random() < 0.85 else 'minecraft:calcite')

# ---- kan şelaleleri (kırmızı cam) ----
for k in range(7):
    a = k * math.tau / 7 + 0.4
    x = int(CX + math.cos(a) * 26.5)
    z = int(CZ + math.sin(a) * 26.5)
    y = TOP
    while y > 2 and has(x, y, z):
        y -= 1
    for yy in range(y, max(0, y - 11), -1):
        if not has(x, yy, z):
            put(x, yy, z, 'minecraft:red_stained_glass')

# ---- kaydet (yapı şablonu NBT) ----
palette, pidx, blocks = [], {}, []
for pos, st in sorted(B.items()):
    if st not in pidx:
        pidx[st] = len(palette)
        palette.append(st)
    blocks.append((pos, pidx[st]))


def s(v):
    b = v.encode('utf-8')
    return struct.pack('>H', len(b)) + b


def tag_name(t, name):
    return bytes([t]) + s(name)


def i32(v):
    return struct.pack('>i', v)


def lst(tid, items):
    return bytes([tid]) + i32(len(items)) + b''.join(items)


out = bytearray()
out += tag_name(10, '')
out += tag_name(9, 'size') + lst(3, [i32(W), i32(H), i32(D)])
pal = []
for (name, props) in palette:
    e = tag_name(8, 'Name') + s(name)
    if props:
        e += tag_name(10, 'Properties') + b''.join(tag_name(8, k) + s(str(v)) for k, v in props) + b'\x00'
    e += b'\x00'
    pal.append(e)
out += tag_name(9, 'palette') + lst(10, pal)
bl = []
for (x, y, z), si in blocks:
    bl.append(tag_name(9, 'pos') + lst(3, [i32(x), i32(y), i32(z)]) + tag_name(3, 'state') + i32(si) + b'\x00')
out += tag_name(9, 'blocks') + lst(10, bl)
out += tag_name(9, 'entities') + bytes([0]) + i32(0)
out += tag_name(3, 'DataVersion') + i32(3465)
out += b'\x00'
dst = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(__file__), '..', 'src/main/resources/data/kizilay/structures/lair/arena.nbt')
os.makedirs(os.path.dirname(dst), exist_ok=True)
with gzip.GzipFile(dst, 'wb', mtime=0) as f:
    f.write(bytes(out))
print('blok', len(blocks), 'palet', len(palette), 'boyut', os.path.getsize(dst))

# ---- önizleme (isometrik) ----
if len(sys.argv) > 2:
    from PIL import Image, ImageDraw
    COL = {'white_wool': (240, 240, 245), 'light_gray_wool': (190, 190, 200), 'gray_wool': (120, 120, 135), 'calcite': (225, 225, 220),
           'blackstone': (40, 36, 44), 'basalt': (60, 60, 66), 'magma_block': (230, 90, 20), 'gilded_blackstone': (200, 160, 40),
           'crying_obsidian': (60, 20, 110), 'polished_blackstone_bricks': (52, 48, 58), 'polished_blackstone': (60, 56, 66),
           'cracked_polished_blackstone_bricks': (46, 42, 52), 'red_nether_bricks': (110, 20, 24), 'gold_block': (250, 215, 60),
           'red_concrete': (160, 30, 30), 'polished_blackstone_brick_slab': (52, 48, 58), 'moon_altar': (255, 0, 255), 'shroomlight': (255, 160, 70),
           'chiseled_polished_blackstone': (70, 66, 80), 'red_stained_glass': (230, 60, 60)}
    S = 7
    im = Image.new('RGB', (1300, 1000), (90, 140, 210))
    dr = ImageDraw.Draw(im)
    az = math.radians(35)
    items = []
    for (x, y, z), (nm, _) in B.items():
        u, v = x - CX, z - CZ
        ru = u * math.cos(az) - v * math.sin(az)
        rv = u * math.sin(az) + v * math.cos(az)
        items.append((rv, ru, y, nm))
    items.sort(key=lambda t: (-t[0], t[2]))
    for rv, ru, y, nm in items:
        c = COL.get(nm.split(':')[1], (255, 0, 255))
        sx = 650 + ru * S
        sy = 640 - y * S * 0.6 - rv * S * 0.6
        dr.rectangle([sx, sy, sx + S, sy + S], fill=c)
    im.save(sys.argv[2])
