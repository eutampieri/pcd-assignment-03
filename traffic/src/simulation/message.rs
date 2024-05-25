use actix::Message;

use super::{car::Car, Distance, Id, Instant, Speed};

#[derive(Debug)]
pub struct Step(pub Instant, pub Option<Distance>);

impl Message for Step {
    type Result = u8;
}

pub struct EnvStep(pub Instant);

impl Message for EnvStep {
    type Result = ();
}
