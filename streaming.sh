## UDP
ffmpeg -s 1280x720 -r 30 -f x11grab  -i :0.0 -codec:v libx264 -g 5  -preset ultrafast -tune zerolatency -b:v 3000k -f mpegts   udp://192.168.42.129:4242

ffplay -probesize 32 -sync ext udp://localhost:4242?listen
ffmpeg -s 1280x720 -r 30 -f x11grab  -i :0.0 -codec:v libx264 -g 5  -preset ultrafast -tune zerolatency -b:v 4000k -f mpegts   udp://192.168.42.129:5454

#ESSAYER  -vpr baseline


## UDP low latency sd
ffmpeg -f x11grab -s 480x360 -framerate 60 -i :0.0 -c:v libx264 -preset ultrafast -tune zerolatency -pix_fmt yuv444p -x264opts crf=20:vbv-maxrate=3000:vbv-bufsize=100:intra-refresh=1:slice-max-size=1500:keyint=25:ref=1:b-adapt=1 -f mpegts udp://192.168.42.129:5454

/ffmpeg -f x11grab -s 480x360 -framerate 60 -i :0.0 -c:v libx264 -preset fast -tune zerolatency -pix_fmt yuv444p -x264opts crf=20:vbv-maxrate=3000:vbv-bufsize=50:intra-refresh=1:slice-max-size=1500:keyint=25:ref=1:bframes=0:b-adapt=1 -f mpegts udp://192.168.42.129:5454
Fin de la conversation

