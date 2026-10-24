# Kızıl Ay: Ay Efendisi (Forge 1.20.1 + GeckoLib 4)

Mod id: `kizilay` · Boss: **Kızıl Ay Kralı** · Yapı: **Kızıl Ay Göksarayı**

## Jar nasıl alınır (3 adım)
1. **Modeli çıkar (hazır, gerekirse yenile).** `python3 tools/bbmodel_to_geckolib.py blockbench/kizil_ay_krali_sahne.bbmodel src/main/resources/assets/kizilay` komutu geo ve animasyon dosyalarını üretir (zaten eklendi). Elle yapmak istersen: Blockbench'te `blockbench/kizil_ay_krali_sahne.bbmodel` dosyasını aç.
   `File > Convert Project` ile Bedrock/GeckoLib biçimine çevir, sonra `File > Export > Export Bedrock Geometry` ve
   `Export Animations` yap. Çıkan dosyaları şu adlarla koy:
   - `src/main/resources/assets/kizilay/geo/kizil_ay_krali.geo.json`
   - `src/main/resources/assets/kizilay/animations/kizil_ay_krali.animation.json`
   (Animasyon adları: spawn, idle, walk, attack_crescent, attack_leap, attack_whirl, phase2_transform, idle_phase2,
   draw_hammer, idle_hammer, hammer_smash, hammer_sweep, hammer_quake, death. `animation.xxx.` ön eki gelirse sil.)
2. **GitHub'a yükle.** Bu klasörün tamamını yeni bir GitHub deposuna yükle.
3. **Actions > "Jar derle"** otomatik çalışır (ya da elle başlat). Bitince sayfanın altındaki **kizilay-jar**
   artifact'ından `kizilay-1.0.0.jar` dosyasını indir. Bilgisayarında JDK 17 + Gradle 8.5 varsa `gradle build` de olur
   (`build/libs/`).

Oyunda GeckoLib 4.4.9 (1.20.1 Forge) kurulu olmalı.

## Oyunda
- Yapıyı bul: `/locate structure kizilay:kizil_ay_goksarayi` → koordinata `/tp @s x 200 z` ile git
  (yapı gökyüzünde, y≈188 civarı; zemin bulut ada).
- Ortadaki **Ay Sunağı**'na sağ tıkla: çekici almaya çalışırsın, geri savrulursun, sunak yere gömülür, boss doğar.
- Elinde sunak olsun istersen: `/give @s kizilay:moon_altar` ya da tarif (altın blok, ağlayan obsidyen, obsidyen, Kızıl Ay Parçası).
- Boss'u çağırma yumurtası: `/summon kizilay:kizil_ay_krali`.
- Faz 1 (kırmızı, tırpan): hilal, sıçrayış, girdap. %50 canda 10.5 sn'lik dönüşüm, sonra faz 2 (mavi, çekiç):
  ezme, süpürme, deprem. Ölünce çekiç düşer, boss dağılır, ganimet son anda düşer.

## Dosya haritası
- `src/main/java/com/kizilay/entity/KizilAyKrali.java` : yapay zekâ, saldırılar, fazlar, ölüm.
- `src/main/java/com/kizilay/block/AltarBlock.java` : sunak (boss'u çağırır).
- `src/main/resources/data/kizilay/worldgen/*` ve `structures/lair/arena.nbt` : gök yapısı.
- `tools/make_lair.py` : yapıyı yeniden üreten betik (boyut, sütun sayısı, renkler değiştirilebilir).
- `blockbench/kamera_yollari.json` : sinematik kamera yolları (oyun içi oynatıcı henüz yazılmadı).
