use actix::{Actor, Context, Handler};

use super::{message::Step, Distance, Id, Identifiable, Speed};

#[derive(Debug, Default)]
pub struct Car {
    id: Id<Self>,
    speed: Speed,
}

impl Car {
    pub fn new(id: Id<Self>) -> Self {
        Self {
            id,
            ..Default::default()
        }
    }
}

impl Actor for Car {
    type Context = Context<Self>;

    fn started(&mut self, _ctx: &mut Self::Context) {
        println!("Car {:?} started!", self.id);
        //System::current().stop(); // <- stop system
    }
}

impl Identifiable for Car {
    fn get_id(&self) -> Id<Self> {
        self.id.clone()
    }
}

impl PartialEq for Car {
    fn eq(&self, other: &Self) -> bool {
        self.id == other.id
    }
}
impl Eq for Car {}

impl std::hash::Hash for Car {
    fn hash<H: std::hash::Hasher>(&self, state: &mut H) {
        self.id.hash(state);
    }
}

impl Handler<Step> for Car {
    type Result = <Step as actix::Message>::Result;

    fn handle(&mut self, msg: Step, ctx: &mut Self::Context) -> Self::Result {
        println!("[{:?}, distance: {:?}] ", self.get_id(), msg.1);
        if !msg.1.map(|x| x < Distance::from(10)).unwrap_or_default() {
            //println!("Car {:?} accellerating!", self.id);
            self.speed.accellerate(Speed::from(20));
        } else {
            //println!("Car {:?} braking!", self.id);
            self.speed.brake();
        }
        self.speed.inner()
    }
}
