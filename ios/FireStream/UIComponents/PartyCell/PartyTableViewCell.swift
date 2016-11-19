//
//  PartyTableViewCell.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import UIKit

class PartyTableViewCell: UITableViewCell {
    @IBOutlet weak var imgAlbumArt: UIImageView!
    @IBOutlet weak var lblName: UILabel!
    @IBOutlet weak var lblHost: UILabel!

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
