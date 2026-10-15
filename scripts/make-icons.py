#!/usr/bin/env python3
"""Gera os ícones da APK (native/res/**). Só é preciso voltar a correr se quiseres mudar o desenho."""
from PIL import Image, ImageDraw
import os

GREEN = (64, 96, 83, 255)      # #406053 — cor da app
WHITE = (255, 255, 255, 255)
GOLD = (200, 151, 58, 255)       # #c8973a — acento da marca
OUT = os.path.join(os.path.dirname(__file__), '..', 'native', 'res')
S = 1024  # tamanho de trabalho

def glyph(draw, scale, cx, cy):
    """Casa com lupa (visita técnica), desenhada num quadro de 100x100 e centrada em (cx, cy)."""
    def P(x, y):
        return (cx + (x - 50) * scale, cy + (y - 50) * scale)
    house = [P(50, 14), P(90, 50), P(80, 50), P(80, 86), P(20, 86), P(20, 50), P(10, 50)]
    draw.polygon(house, fill=WHITE)
    # lupa dentro do corpo da casa: aro + cabo (cabo a dourado para distinguir da app da Vistoria)
    lcx, lcy, r = 44, 63, 11
    w = max(2, int(6 * scale))
    (x0, y0), (x1, y1) = P(lcx - r, lcy - r), P(lcx + r, lcy + r)
    draw.ellipse([x0, y0, x1, y1], outline=GREEN, width=w)
    a = P(lcx + r * 0.72, lcy + r * 0.72)
    b = P(lcx + r * 0.72 + 11, lcy + r * 0.72 + 11)
    draw.line([a, b], fill=GOLD, width=w + int(1.5 * scale))
    for x, y in (b,):                              # ponta arredondada do cabo
        rr = (w + int(1.5 * scale)) / 2
        draw.ellipse([x - rr, y - rr, x + rr, y + rr], fill=GOLD)

def legacy_square():
    im = Image.new('RGBA', (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rounded_rectangle([0, 0, S - 1, S - 1], radius=int(S * 0.22), fill=GREEN)
    glyph(d, scale=S * 0.62 / 80, cx=S / 2, cy=S / 2 + S * 0.01)
    return im

def legacy_round():
    im = Image.new('RGBA', (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.ellipse([0, 0, S - 1, S - 1], fill=GREEN)
    glyph(d, scale=S * 0.54 / 80, cx=S / 2, cy=S / 2 + S * 0.01)
    return im

def adaptive_foreground():
    im = Image.new('RGBA', (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    glyph(d, scale=S * 0.46 / 80, cx=S / 2, cy=S / 2 + S * 0.01)   # dentro da zona segura (66 %)
    return im

dens = {'mdpi': 1.0, 'hdpi': 1.5, 'xhdpi': 2.0, 'xxhdpi': 3.0, 'xxxhdpi': 4.0}
sq, rd, fg = legacy_square(), legacy_round(), adaptive_foreground()
for name, f in dens.items():
    folder = os.path.join(OUT, f'mipmap-{name}')
    os.makedirs(folder, exist_ok=True)
    sz = int(round(48 * f)); fz = int(round(108 * f))
    sq.resize((sz, sz), Image.LANCZOS).save(os.path.join(folder, 'ic_launcher.png'))
    rd.resize((sz, sz), Image.LANCZOS).save(os.path.join(folder, 'ic_launcher_round.png'))
    fg.resize((fz, fz), Image.LANCZOS).save(os.path.join(folder, 'ic_launcher_foreground.png'))

# ecrã de arranque: cor lisa (fica esticado); e um ícone grande para referência
os.makedirs(OUT, exist_ok=True)
Image.new('RGB', (64, 64), GREEN[:3]).save(os.path.join(OUT, 'splash.png'))
sq.resize((512, 512), Image.LANCZOS).save(os.path.join(OUT, 'icon-512.png'))
print('Ícones gerados em', os.path.abspath(OUT))
