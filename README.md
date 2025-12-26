# VirnorWatchdog Plugin

Minecraft 1.21.1 için geliştirilmiş güçlü bir moderasyon ve oyuncu izleme plugini.

## Özellikler

### 🔍 Spectator Modu
- `/wd spec <oyuncu>` komutu ile oyuncuları gizlice izleyin
- Vanish modda uçarak oyuncuları takip edin
- Essentials vanish kullanan oyuncular sizi göremez
- Vanish modundaki oyuncuları görebilirsiniz
- Oyuncularla etkileşime giremezsiniz (hasar veremez/alamazsınız)

### 🔨 Ban Sistemi
- Özelleştirilebilir ban sebepleri ve süreleri
- GUI üzerinden kolay ban işlemleri
- Otomatik komut çalıştırma (sunucu ban komutu kullanır)
- Configden ayarlanabilir süreler
- Ban geçmişi kaydı

### 🔇 Mute Sistemi
- Özelleştirilebilir mute sebepleri ve süreleri
- GUI üzerinden kolay mute işlemleri
- Otomatik komut çalıştırma (sunucu mute komutu kullanır)
- Configden ayarlanabilir süreler
- Mute geçmişi kaydı

### 📊 Oyuncu Geçmişi
- Tüm ban ve mute kayıtları
- Tarih, yetkili ve sebep bilgileri
- Sayfalanmış görüntüleme
- Toplam ceza istatistikleri

## Komutlar

| Komut | Açıklama | Yetki |
|-------|----------|-------|
| `/wd spec <oyuncu>` | Oyuncuyu izlemeye başla/bitir | `virnorwatchdog.spec` |
| `/wd ban <oyuncu>` | Ban menüsünü aç | `virnorwatchdog.ban` |
| `/wd mute <oyuncu>` | Mute menüsünü aç | `virnorwatchdog.mute` |
| `/wd history <oyuncu>` | Oyuncu geçmişini görüntüle | `virnorwatchdog.history` |

## Yetkiler

- `virnorwatchdog.spec` - Oyuncu izleme
- `virnorwatchdog.ban` - Ban atma
- `virnorwatchdog.mute` - Mute atma
- `virnorwatchdog.history` - Geçmiş görüntüleme
- `virnorwatchdog.admin` - Tüm yetkiler

## Kurulum

1. Plugin JAR dosyasını `plugins` klasörüne atın
2. Sunucuyu başlatın
3. `plugins/VirnorWatchdog/config.yml` dosyasını düzenleyin
4. `/reload` veya sunucuyu yeniden başlatın

## Yapılandırma

### Ban Sebepleri
Config dosyasında her ban sebebi için:
- Görünen isim
- Süre (dakika cinsinden, 0 = kalıcı)
- Çalıştırılacak komut

```yaml
ban-reasons:
  killaura:
    display-name: "&cKillAura"
    duration: 4320  # 3 gün
    command: "ban {player} {duration} {reason}"
```

### Mute Sebepleri
Config dosyasında her mute sebebi için:
- Görünen isim
- Süre (dakika cinsinden)
- Çalıştırılacak komut

```yaml
mute-reasons:
  spam:
    display-name: "&eSpam"
    duration: 60  # 1 saat
    command: "mute {player} {duration} {reason}"
```

### Spectator Ayarları
```yaml
spectator:
  can-see-vanished: true  # Vanish'li oyuncuları görebilir
  visible-to-essentials-vanish: false  # Essentials vanish kullananlar sizi göremez
  fly-speed: 2.0  # Uçuş hızı
  open-inventory: false  # Envanter açabilir mi
```

## Bağımlılıklar

- **Minecraft**: 1.21.1
- **Java**: 21
- **Essentials** (Opsiyonel): Vanish entegrasyonu için

## Derleme

```bash
mvn clean package
```

JAR dosyası `target/VirnorWatchdog-1.0.0.jar` konumunda oluşturulacaktır.

## Özellikler

✅ Spectator modu ile gizli izleme  
✅ Essentials vanish entegrasyonu  
✅ GUI tabanlı ban/mute sistemi  
✅ Komut çalıştırma (scriptler)  
✅ Ceza geçmişi kayıt sistemi  
✅ Özelleştirilebilir süreler  
✅ Çoklu dil desteği (mesajlar config'de)  
✅ Sayfalanmış geçmiş görüntüleme  

## Destek

Sorunlar veya öneriler için issue açabilirsiniz.

## Lisans

Bu plugin Virnor tarafından geliştirilmiştir.
