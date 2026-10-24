"""bbmodel (Blockbench 'free' format) -> GeckoLib/Bedrock geo.json + animation.json
Kurallar Blockbench'in Bedrock kodundan birebir alinmistir (bedrock.js compileCube/compileGroup,
bedrock_animation.js compileAnimation, keyframe.js compileBedrockKeyframe):
  pivot/origin x ters, bone rotation (-x,-y,z), cube origin x = -(from.x+size.x), anim position x ters,
  anim rotation (-x,-y,z), scale aynen.
Kullanim: python3 tools/bbmodel_to_geckolib.py blockbench/kizil_ay_krali_sahne.bbmodel src/main/resources/assets/kizilay"""
import json, sys, os

def r(v, n=4):
    v = round(v, n)
    return 0.0 if v == 0 else v

def main(src, outdir, ident='kizil_ay_krali'):
    b = json.load(open(src))
    cubes = {e['uuid']: e for e in b['elements'] if e.get('type', 'cube') == 'cube'}
    bones, group_by_uuid, names = [], {}, set()

    def walk(node, parent):
        if not isinstance(node, dict):
            return
        name = node['name']
        assert name not in names, 'ayni isimli grup: ' + name
        names.add(name)
        group_by_uuid[node['uuid']] = name
        o = node['origin']
        bone = {'name': name}
        if parent:
            bone['parent'] = parent
        bone['pivot'] = [r(-o[0]), r(o[1]), r(o[2])]
        rot = node.get('rotation', [0, 0, 0])
        if any(rot):
            bone['rotation'] = [r(-rot[0]), r(-rot[1]), r(rot[2])]
        cl = []
        for ch in node.get('children', []):
            if isinstance(ch, str):
                c = cubes.get(ch)
                if not c:
                    continue
                f, t = c['from'], c['to']
                size = [t[0] - f[0], t[1] - f[1], t[2] - f[2]]
                cube = {'origin': [r(-(f[0] + size[0])), r(f[1]), r(f[2])], 'size': [r(s) for s in size]}
                cr = c.get('rotation', [0, 0, 0])
                if any(cr):
                    co = c['origin']
                    cube['pivot'] = [r(-co[0]), r(co[1]), r(co[2])]
                    cube['rotation'] = [r(-cr[0]), r(-cr[1]), r(cr[2])]
                if c.get('inflate'):
                    cube['inflate'] = c['inflate']
                assert c.get('box_uv', True), 'sadece box_uv destekli'
                cube['uv'] = c['uv_offset']
                if c.get('mirror_uv'):
                    cube['mirror'] = True
                cl.append(cube)
        if cl:
            bone['cubes'] = cl
        bones.append(bone)
        for ch in node.get('children', []):
            walk(ch, name)

    for n in b['outliner']:
        walk(n, None)

    geo = {'format_version': '1.12.0', 'minecraft:geometry': [{
        'description': {'identifier': 'geometry.' + ident, 'texture_width': b['resolution']['width'],
                        'texture_height': b['resolution']['height'], 'visible_bounds_width': 8,
                        'visible_bounds_height': 8, 'visible_bounds_offset': [0, 2, 0]},
        'bones': bones}]}

    anims = {}
    for a in b['animations']:
        t = {}
        if a.get('loop') == 'hold':
            t['loop'] = 'hold_on_last_frame'
        elif a.get('loop') == 'loop':
            t['loop'] = True
        t['animation_length'] = r(a['length'])
        bt, snd = {}, {}
        for uid, an in a['animators'].items():
            kfs = an.get('keyframes', [])
            if an.get('type') == 'effect':
                for k in sorted([k for k in kfs if k['channel'] == 'sound'], key=lambda k: k['time']):
                    pts = [{'effect': d['effect']} for d in k['data_points'] if d.get('effect')]
                    if pts:
                        snd[str(r(k['time']))] = pts[0] if len(pts) == 1 else pts
                continue
            name = group_by_uuid.get(uid)
            if name is None:
                continue
            entry = {}
            for ch in ('rotation', 'position', 'scale'):
                ks = sorted([k for k in kfs if k['channel'] == ch], key=lambda k: k['time'])
                if not ks:
                    continue
                d = {}
                for k in ks:
                    p = k['data_points'][0]
                    v = [p['x'], p['y'], p['z']]
                    if ch == 'position':
                        v[0] = -v[0]
                    elif ch == 'rotation':
                        v[0] = -v[0]
                        v[1] = -v[1]
                    d[str(r(k['time']))] = [r(x) for x in v]
                entry[ch] = list(d.values())[0] if len(d) == 1 else d
            if entry:
                bt[name] = entry
        if bt:
            t['bones'] = bt
        if snd:
            t['sound_effects'] = snd
        anims[a['name']] = t

    os.makedirs(os.path.join(outdir, 'geo'), exist_ok=True)
    os.makedirs(os.path.join(outdir, 'animations'), exist_ok=True)
    json.dump(geo, open(os.path.join(outdir, 'geo', ident + '.geo.json'), 'w'), separators=(',', ':'))
    json.dump({'format_version': '1.8.0', 'animations': anims}, open(os.path.join(outdir, 'animations', ident + '.animation.json'), 'w'), separators=(',', ':'))
    print('kemik', len(bones), 'kup', sum(len(x.get('cubes', [])) for x in bones), 'animasyon', len(anims))
    bn = {x['name'] for x in bones}
    for n, a in anims.items():
        miss = [k for k in a.get('bones', {}) if k not in bn]
        assert not miss, (n, miss)

if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
