sudo ipfw flush

echo ""
echo "PLR: " $1
echo ""

sudo ipfw add pipe 1 all from 127.0.0.1 to 127.0.0.2 out
sudo ipfw add pipe 2 all from 127.0.0.2 to 127.0.0.1 out
sudo ipfw pipe 1 config bw 20000bps delay 50ms plr $1 
sudo ipfw pipe 2 config bw 20000bps delay 50ms plr 0.0

sudo ipfw show
sudo ipfw pipe show
